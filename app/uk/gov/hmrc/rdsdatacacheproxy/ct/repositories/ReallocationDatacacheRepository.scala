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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ReallocationRow

import java.sql.ResultSet
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import scala.collection.mutable.ListBuffer

@ImplementedBy(classOf[ReallocationDatacacheRepositoryImpl])
trait ReallocationDatacacheRepository {
  def getByAccountingPeriod(taxRef: Long, accPeriod: Long): Future[Seq[ReallocationRow]]
}

class ReallocationDatacacheRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends ReallocationDatacacheRepository
    with Logging {

  def getByAccountingPeriod(taxRef: Long, accPeriod: Long): Future[Seq[ReallocationRow]] = {
    logger.info(s"GetByAccountingPeriod::Input request: taxRef, accPeriod: <$taxRef>, <$accPeriod>")
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call CT_DC_PK.getReallocationsTo(?, ?, ?)}")
        try {
          storedProcedure.setLong(1, taxRef)
          storedProcedure.setLong(2, accPeriod)
          storedProcedure.registerOutParameter(3, OracleTypes.CURSOR)

          storedProcedure.execute()

          val reallocationsRs = storedProcedure.getObject(3, classOf[ResultSet])
          Option(reallocationsRs).map(readReallocations).getOrElse(List.empty)
        } finally {
          storedProcedure.close()
        }
      }
    }
  }

  private def readReallocations(rs: ResultSet): List[ReallocationRow] = {
    val buffer = ListBuffer[ReallocationRow]()
    while (rs.next()) {
      buffer += ReallocationRow(
        amount                  = rs.getBigDecimal("AMOUNT"),
        reallocationDate        = Option(rs.getDate("reallocation_date")).map(_.toLocalDate).get,
        sourceApEndDate         = Option(rs.getDate("source_ap_end_date")).map(_.toLocalDate),
        sourceTaxpayerReference = rs.getString("source_taxpayer_reference")
      )
    }
    buffer.toList
  }

}
