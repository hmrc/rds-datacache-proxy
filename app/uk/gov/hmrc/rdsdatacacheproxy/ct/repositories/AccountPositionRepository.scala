/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{AccountPositionResponse, ApAmountItem}

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AccountPositionRepositoryImpl])
trait AccountPositionRepository {
  def getAccountPosition(taxRef: Long): Future[Option[AccountPositionResponse]]
}

class AccountPositionRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends AccountPositionRepository
    with Logging {

  def getAccountPosition(taxRef: Long): Future[Option[AccountPositionResponse]] = {
    logger.info(s"Input request: ")
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("{call CT_LNP_PK.getAccountPosition(?, ?, ?, ?, ?, ?, ?)}")
        try {
          cs.setLong(1, taxRef)

          cs.registerOutParameter(2, java.sql.Types.NUMERIC) // p_Amount_Due
          cs.registerOutParameter(3, java.sql.Types.VARCHAR) // p_GPA_Link_Flag
          cs.registerOutParameter(4, java.sql.Types.DATE) // p_As_On_Date

          cs.registerOutParameter(5, OracleTypes.CURSOR) // p_Taxpayer_List :: CURSOR
          cs.registerOutParameter(6, OracleTypes.CURSOR) // p_AP_Amounts :: CURSOR
          cs.registerOutParameter(7, java.sql.Types.VARCHAR) // p_Does_Company_Exist

          cs.execute()

          val taxpayerListRds = cs.getObject(5, classOf[ResultSet])
          val aPAmountsRds = cs.getObject(6, classOf[ResultSet])

          val taxpayerList = Option(taxpayerListRds).map(readTaxRefList).getOrElse(List.empty)

          val aPAmounts = Option(aPAmountsRds).map(readApAmounts).getOrElse(List.empty)

          Some(
            AccountPositionResponse(
              amountDue        = Option(cs.getBigDecimal(2)),
              asOnDate         = Option(cs.getDate(4)).map(_.toLocalDate),
              gpaLinkFlag      = Option(cs.getString(3)),
              taxpayerList     = taxpayerList,
              apAmounts        = aPAmounts,
              doesCompanyExist = Option(cs.getString(7))
            )
          )

        } catch {
          // sql.SQLException: ORA-01422: exact fetch returns more than requested number of rows
          case sqlException: java.sql.SQLException if sqlException.getMessage.contains("exact fetch returns more than requested number of rows") =>
            logger.info("No data found")
            None // no other exceptions to be caught
        } finally {
          cs.close()
        }
      }
    }
  }

  private def readApAmounts(rs: ResultSet): List[ApAmountItem] = {
    val buffer = ListBuffer[ApAmountItem]()
    while (rs.next()) {
      buffer += ApAmountItem(
        accountingPeriod = Option(rs.getLong("Accounting_period")).get,
        apEndDate        = Option(rs.getDate("AP_End_date")).map(_.toLocalDate),
        amountDueForAp   = Option(rs.getBigDecimal("Amount_Due_For_AP")),
        apStatus         = Option(rs.getString("AP_Status"))
      )
    }
    buffer.toList
  }

  private def readTaxRefList(rs: ResultSet): List[String] = {
    val buffer = ListBuffer[String]()
    while (rs.next()) {
      buffer += Option(rs.getLong("taxpayer_reference")).map(_.toString).get
    }
    buffer.toList
  }

}
