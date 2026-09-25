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
import play.api.db.Database
import play.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{GpaPaymentsDetails, GroupReferenceNumberLstItem, GroupSummaryDetails, GroupSummaryDetailsItem}

import java.sql.ResultSet
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[GroupPaymentsRepositoryImpl])
trait GroupPaymentsRepository {
  def getGroupSummary(gpaUTR: Long, nomCompanyUTR: Long): Future[Option[GroupSummaryDetails]]

  def getPaymentsDetails(gpaUTR: Long, contractVersion: Int, startIndex: Int, count: Int): Future[GpaPaymentsDetails]
}

class GroupPaymentsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends GroupPaymentsRepository
    with RepositoryDataSupport
    with Logging {

  override def getGroupSummary(gpaUTR: Long, nomCompanyUTR: Long): Future[Option[GroupSummaryDetails]] = {
    logger.info(
      s"Retrieving getGroupSummary: $gpaUTR - $nomCompanyUTR"
    )
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("{call CT_GPA_PK.getGPAGroupSummary(?, ?, ?, ?, ?)}")

        try {
          cs.setLong(1, gpaUTR)
          cs.setLong(2, nomCompanyUTR)

          cs.registerOutParameter(3, OracleTypes.CURSOR)
          cs.registerOutParameter(4, OracleTypes.CURSOR)

          cs.registerOutParameter(5, java.sql.Types.VARCHAR) // pNOMINATED_COMPANY_NAME

          cs.execute()

          val curSummaryRds = cs.getObject(3, classOf[ResultSet])
          val groupRefsRds = cs.getObject(4, classOf[ResultSet])

          val curSummary = Option(curSummaryRds).map(readCurSummary).getOrElse(List.empty)
          val groupRefs = Option(groupRefsRds).map(readGroupRefs).getOrElse(List.empty)

          Some(
            GroupSummaryDetails(
              gpaGrpSummaryDetails  = curSummary,
              gpaReferenceNumberLst = groupRefs,
              nominatedCompanyName  = cs.getString(5)
            )
          )
        } catch {
          case sqlException: java.sql.SQLException if sqlException.getMessage.contains("no data found") =>
            logger.info("No data found")
            None // no other exceptions to be caught
        } finally {
          cs.close()
        }
      }
    }
  }

  private def readGroupRefs(rs: ResultSet): List[GroupReferenceNumberLstItem] = {
    val buffer = ListBuffer[GroupReferenceNumberLstItem]()
    while (rs.next()) {
      buffer += GroupReferenceNumberLstItem(
        taxpayerReference = Option(rs.getLong("taxpayer_reference")).get
      )
    }
    buffer.toList
  }

  private def readCurSummary(rs: ResultSet): List[GroupSummaryDetailsItem] = {
    val buffer = ListBuffer[GroupSummaryDetailsItem]()
    while (rs.next()) {
      buffer += GroupSummaryDetailsItem(
        contractEndDate         = Option(rs.getDate("CONTRACT_END_DATE")).map(_.toLocalDate).get,
        groupTaxCharge          = Option(rs.getBigDecimal("GROUP_TAX_CHARGE")),
        groupPayment            = Option(rs.getBigDecimal("GROUP_PAYMENT")),
        groupPaymentRecordCount = Option(rs.getInt("GROUP_PAYMENT_RECORD_COUNT")).get,
        contractStatus          = Option(rs.getString("CONTRACT_STATUS")).get,
        contractVersion         = Option(rs.getInt("CONTRACT_VERSION")).get
      )
    }
    buffer.toList
  }

  private val paymentDetailsRec = GpaPaymentsDetails(
    gpaPayments            = List.empty,
    totalNumOfRecords      = None,
    gppEndDate             = None,
    gppTotalGroupPayment   = None,
    gppTotalGroupTax       = None,
    gppStatus              = "ACTIVE",
    gppCni                 = None,
    gppApportionmentMethod = "METHOD"
  )

  override def getPaymentsDetails(gpaUTR: Long, contractVersion: Int, startIndex: Int, count: Int): Future[GpaPaymentsDetails] = {
    Future.successful(
      paymentDetailsRec
    )
  }
}
