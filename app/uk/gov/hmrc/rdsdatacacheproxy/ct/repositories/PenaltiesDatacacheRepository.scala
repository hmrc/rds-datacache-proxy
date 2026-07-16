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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PenaltyTransaction
import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PenaltiesDatacacheRepositoryImpl])
trait PenaltiesDatacacheRepository {
  def getPenaltyTransactionList(taxRef: Long, accPeriod: Long): Future[List[PenaltyTransaction]]
}

class PenaltiesDatacacheRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends PenaltiesDatacacheRepository
    with Logging {

  def getPenaltyTransactionList(taxRef: Long, accPeriod: Long): Future[List[PenaltyTransaction]] = {
    logger.info(s"Input request: taxRef, accPeriod: <$taxRef>, <$accPeriod>")
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call CT_DC_PK.getPenaltyTransactionList(?, ?, ?)}")

        storedProcedure.setLong(1, taxRef)
        storedProcedure.setLong(2, accPeriod)
        storedProcedure.registerOutParameter(3, OracleTypes.CURSOR) // p_list

        storedProcedure.execute()

        val pListRs = storedProcedure.getObject(3, classOf[ResultSet])

        try {
          val penalties = Option(pListRs).map(readPenaltiesTransaction).getOrElse(List.empty)
          penalties
        } finally {
          storedProcedure.close()
        }
      }
    }
  }

  private def readPenaltiesTransaction(rs: ResultSet): List[PenaltyTransaction] = {
    val buffer = ListBuffer[PenaltyTransaction]()
    while (rs.next()) {
      buffer += PenaltyTransaction(
        penaltyDate   = Option(rs.getDate("penalty_date")).map(_.toLocalDate).get,
        `type`        = rs.getString("type"),
        postingAmount = rs.getBigDecimal("posting_amount")
      )
    }
    buffer.toList
  }

}
