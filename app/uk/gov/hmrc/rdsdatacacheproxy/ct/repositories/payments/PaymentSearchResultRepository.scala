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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentSearchResult, PaymentSearchResultResponse}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepositoryDataSupport

import java.sql.{Connection, Date, ResultSet, Types}
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PaymentSearchResultRepositoryImpl])
trait PaymentSearchResultRepository {
  def getPaymentSearchResult(taxRef: Long, fromDate: Option[LocalDate], toDate: Option[LocalDate]): Future[PaymentSearchResultResponse]
}

class PaymentSearchResultRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends PaymentSearchResultRepository
    with RepositoryDataSupport
    with Logging {

  override def getPaymentSearchResult(taxRef: Long, fromDate: Option[LocalDate], toDate: Option[LocalDate]): Future[PaymentSearchResultResponse] =
    logger.info(s"Invoking stored procedure to retrieve PaymentSearchResultResponse for taxRef :: $taxRef")
    Future {
      db.withConnection { connection =>
        retrievePaymentSearchResult(connection, taxRef, fromDate, toDate)
      }
    }

  private def processPaymentSearchResult(rs: ResultSet): PaymentSearchResult =
    PaymentSearchResult(
      accountingPeriod       = Option(rs.getBigDecimal("ACCOUNTING_PERIOD")),
      payslipId              = Option(rs.getBigDecimal("PAYSLIP_ID")),
      amount                 = Option(rs.getBigDecimal("AMOUNT")),
      effectiveDateOfPayment = Option(rs.getDate("EFFECTIVE_DATE_OF_PAYMENT").toLocalDate),
      paymentType            = Option(rs.getString("PAYMENT_TYPE")),
      bankSortCode           = Option(rs.getBigDecimal("BANK_SORT_CODE")),
      bankAccountNumber      = Option(rs.getBigDecimal("BANK_ACCOUNT_NUMBER")),
      chequeNumber           = Option(rs.getBigDecimal("CHEQUE_NUMBER")),
      paymentAmount          = Option(rs.getBigDecimal("PAYMENT_AMOUNT")),
      chequeId               = Option(rs.getBigDecimal("CHEQUE_ID"))
    )

  private def retrievePaymentSearchResult(connection: Connection,
                                          taxRef: Long,
                                          fromDate: Option[LocalDate],
                                          toDate: Option[LocalDate]
                                         ): PaymentSearchResultResponse = {
    val context: String = s"Retrieving PaymentSearchResults"

    val cs = connection.prepareCall("{call CT_DC_PK.getPaymentSearchResults(?,?,?,?)}")

    try {
      cs.setLong(1, taxRef)

      fromDate match {
        case Some(date) =>
          cs.setDate(2, Date.valueOf(date))
        case None =>
          cs.setNull(2, Types.DATE)
      }

      toDate match {
        case Some(date) =>
          cs.setDate(3, Date.valueOf(date))
        case None =>
          cs.setNull(3, Types.DATE)
      }

      cs.registerOutParameter(4, OracleTypes.CURSOR)

      cs.execute()

      val paymentSearchResult: List[PaymentSearchResult] =
        processResultSetList(cs, 4, processPaymentSearchResult, context)

      PaymentSearchResultResponse(
        paymentSearchResult = paymentSearchResult
      )

    } finally {
      cs.close()
    }
  }
}
