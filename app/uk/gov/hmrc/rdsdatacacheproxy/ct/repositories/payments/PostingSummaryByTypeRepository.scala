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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PostingSummaryByTypeResponse, PostingSummaryByTypeRow}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepositoryDataSupport

import java.sql.ResultSet
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PostingSummaryByTypeRepositoryImpl])
trait PostingSummaryByTypeRepository {
  def getPostingSummaryByType(taxRef: Long, postingType: String): Future[PostingSummaryByTypeResponse]
}

class PostingSummaryByTypeRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends PostingSummaryByTypeRepository
    with RepositoryDataSupport
    with Logging {

  override def getPostingSummaryByType(taxRef: Long, postingType: String): Future[PostingSummaryByTypeResponse] =
    val context: String = s"Retrieving PostingSummaryByType"
    logger.info(s"Invoking stored procedure to retrieve PostingSummaryByTypeResponse for taxRef :: $taxRef :: postingType:: $postingType")
    Future {
      db.withConnection { connection =>
        val sp = connection.prepareCall("{call CT_DC_PK.getPostingSummaryByType(?,?,?)}")

        try {
          sp.setLong(1, taxRef)
          sp.setString(2, postingType)
          sp.registerOutParameter(3, OracleTypes.CURSOR)

          sp.execute()

          val postingSummaryByTypeRows: List[PostingSummaryByTypeRow] = processResultSetList(sp, 3, retrievePostingSummaryByType, context)
          PostingSummaryByTypeResponse(
            postingSummaryByTypeResponse = postingSummaryByTypeRows
          )

        } finally {
          sp.close()
        }
      }
    }

  private def retrievePostingSummaryByType(rs: ResultSet): PostingSummaryByTypeRow =
    PostingSummaryByTypeRow(
      accountingPeriod     = Option(rs.getBigDecimal("ACCOUNTING_PERIOD")),
      settlementSequenceNo = Option(rs.getBigDecimal("SETTLEMENT_SEQUENCE_NO")),
      postingAmountSum     = Option(rs.getBigDecimal("POSTING_AMOUNT_SUM"))
    )

}
