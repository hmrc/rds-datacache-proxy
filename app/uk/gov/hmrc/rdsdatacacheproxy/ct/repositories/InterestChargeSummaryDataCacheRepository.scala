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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestCharges, InterestChargesResponse}

import java.sql.{CallableStatement, Connection, ResultSet}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[InterestChargeSummaryDataCacheRepositoryImpl])
trait InterestChargeSummaryDataCacheRepository {
  def getInterestSummary(request: String): Future[InterestCharges]
}

class InterestChargeSummaryDataCacheRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends InterestChargeSummaryDataCacheRepository
    with Logging {

  override def getInterestSummary(taxPayerReference: String): Future[InterestCharges] = {

    logger.info(s"[InterestChargeSummaryDataCacheRepository][getInterestSummary] taxPayerReference: $taxPayerReference")

    Future {
      db.withConnection { conn =>
        callGetInterestSummary(
          conn,
          taxPayerReference.toLong
        )
      }
    }
  }

  private def processInterestChargesResponse(rs: ResultSet): InterestChargesResponse =
    InterestChargesResponse(
      accountingPeriod      = rs.getBigDecimal("ACCOUNTING_PERIOD"),
      interestChargeSummary = rs.getBigDecimal("INTEREST_CHARGE_SUM")
    )

  private def processResultSetList[T](cs: CallableStatement, position: Int, processor: ResultSet => T): List[T] = {
    val rs = cs.getObject(position, classOf[ResultSet])
    try
      if (rs != null) {
        val buffer = scala.collection.mutable.ListBuffer[T]()
        while (rs.next())
          buffer += processor(rs)
        buffer.toList
      } else List.empty
    finally
      if (rs != null) rs.close()
  }

  private def callGetInterestSummary(conn: Connection, taxPayerReference: Long): InterestCharges = {
    val cs = conn.prepareCall(
      "{call CT_DC_PK.getInterestChargeSummary(?, ?) }"
    )
    try {
      cs.setLong(1, taxPayerReference)

      cs.registerOutParameter(2, OracleTypes.CURSOR)

      cs.execute()

      val interestChargesResponse: List[InterestChargesResponse] = processResultSetList(cs, 2, processInterestChargesResponse)

      InterestCharges(
        interestCharges = interestChargesResponse
      )
    } finally {
      cs.close()
    }

  }
}
