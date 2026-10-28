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
import play.api.db.Database
import play.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentPostingResponse, PaymentPostingRow}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepositoryDataSupport

import java.sql.{Connection, ResultSet}
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PaymentAllocationChequeDetailsRepositoryImpl])
trait PaymentPostingsRepository {
  def getPaymentPostings(taxRef: Long): Future[PaymentPostingResponse]

}

class PaymentPostingsRepositoryImpl @Inject() (@NamedDatabase("ct-core") db: Database)(implicit ec: ExecutionContext)
    extends PaymentPostingsRepository
    with Logging
    with RepositoryDataSupport {

  override def getPaymentPostings(taxRef: Long): Future[PaymentPostingResponse] = {
    Future {
      db.withConnection { conn =>
        getPaymentPostings(conn, taxRef)
      }
    }
  }

  private def getPaymentPostings(connection: Connection, taxRef: Long): PaymentPostingResponse = {
    val cs = connection.prepareCall("{call CT_DC_PK.getPaymentPostingList(?,?)}")
    try {
      cs.setLong(1, taxRef)
      cs.registerOutParameter(2, OracleTypes.CURSOR)

      cs.execute()

      val results = cs.getObject(2, classOf[ResultSet]);
      val paymentPostings = Option(results).map(readPaymentPostings).getOrElse(List.empty)

      PaymentPostingResponse(
        paymentPostings = paymentPostings
      )

    } finally {
      cs.close()
    }
  }

  private def readPaymentPostings(rs: ResultSet): List[PaymentPostingRow] = {
    val buffer = ListBuffer[PaymentPostingRow]()
    while (rs.next()) {
      buffer += PaymentPostingRow(
        accountingPeriod       = rs.getLong("ACCOUNTING_PERIOD"),
        settlementSequenceNo   = rs.getLong("SETTLEMENT_SEQUENCE_NO"),
        payslipId              = rs.getLong("SETTLEMENT_SEQUENCE_NO"),
        postingAmount          = rs.getBigDecimal("POSTING_AMOUNT"),
        effectiveDateOfPayment = rs.getDate("EFFECTIVE_DATE_OF_PAYMENT").toLocalDate,
        paymentType            = rs.getString("PAYMENT_TYPE")
      )
    }
    buffer.toList
  }
}
