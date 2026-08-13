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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{ReallocationFromAccDetails, ReallocationFromAccPeriod}

import java.sql.{Connection, ResultSet}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ReallocationFromAccPeriodRepositoryImpl])
trait ReallocationFromAccPeriodRepository {
  def getReallocationFromAccPeriod(taxPayerReference: Long, accountingPeriod: Long): Future[ReallocationFromAccPeriod]
}

class ReallocationFromAccPeriodRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends ReallocationFromAccPeriodRepository
    with RepositoryDataSupport
    with Logging {

  override def getReallocationFromAccPeriod(taxPayerReference: Long, accountingPeriod: Long): Future[ReallocationFromAccPeriod] = {
    logger.info(
      s"[ReallocationFromAccPeriodRepository][getReallocationFromAccPeriod] taxPayerReference: $taxPayerReference, accountingPeriod: $accountingPeriod"
    )
    Future {
      db.withConnection { connection =>
        retrieveReallocationFromDB(connection, taxPayerReference, accountingPeriod)
      }
    }
  }

  private def processReallocationFromAccPeriod(rs: ResultSet): ReallocationFromAccDetails =
    ReallocationFromAccDetails(
      amount                       = Option(rs.getBigDecimal("AMOUNT")),
      reallocationDate             = rs.getDate("REALLOCATION_DATE").toLocalDate,
      destinationApEndDate         = Option(rs.getDate("DESTINATION_AP_END_DATE")).map(_.toLocalDate),
      destinationTaxPayerReference = rs.getString("DESTINATION_TAXPAYER_REFERENCE")
    )

  private def retrieveReallocationFromDB(conn: Connection, taxRef: Long, accPeriod: Long): ReallocationFromAccPeriod = {
    val context: String = s"[ReallocationFromAccPeriodRepository][getReallocationFromAccPeriod]"

    val cs = conn.prepareCall("{call CT_DC_PK.getReallocationsFrom(?, ?, ?)}")
    try {
      cs.setLong(1, taxRef)
      cs.setLong(2, accPeriod)

      cs.registerOutParameter(3, OracleTypes.CURSOR)

      cs.execute()

      val reallocationFromAccDetailsResponse: List[ReallocationFromAccDetails] =
        processResultSetList(cs, 3, processReallocationFromAccPeriod, context)

      ReallocationFromAccPeriod(
        reallocation = reallocationFromAccDetailsResponse
      )

    } finally {
      cs.close()
    }

  }
}
