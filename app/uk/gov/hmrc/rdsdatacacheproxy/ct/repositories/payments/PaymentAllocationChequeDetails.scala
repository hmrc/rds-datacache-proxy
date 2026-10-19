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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentAllocationChequeDetails, PaymentAllocationChequeRow}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepositoryDataSupport

import java.sql.{Connection, ResultSet}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PaymentAllocationChequeDetailsRepositoryImpl])
trait PaymentAllocationChequeDetailsRepository {
  def getPaymentAllocationChequeDetails(taxRef: Long, chequeId: Long): Future[PaymentAllocationChequeDetails]

}

class PaymentAllocationChequeDetailsRepositoryImpl @Inject() (@NamedDatabase("ct-core") db: Database)(implicit ec: ExecutionContext)
    extends PaymentAllocationChequeDetailsRepository
    with Logging
    with RepositoryDataSupport {

  override def getPaymentAllocationChequeDetails(taxRef: Long, chequeId: Long): Future[PaymentAllocationChequeDetails] = {
    Future {
      db.withConnection { conn =>
        retrievePaymentAllocationChequeDetails(conn, taxRef, chequeId)
      }
    }
  }

  private def retrievePaymentAllocationChequeDetails(connection: Connection, taxRef: Long, chequeId: Long): PaymentAllocationChequeDetails = {
    val context = s"Retrieving Payment Allocation Cheque Details"
    val cs = connection.prepareCall("{call CT_DC_PK.getPayAllocationChequeDtls(?,?,?)}")
    try {
      cs.setLong(1, taxRef)
      cs.setLong(2, chequeId)
      cs.registerOutParameter(3, OracleTypes.CURSOR)

      cs.execute()

      val paymentAllocationChequeRow: List[PaymentAllocationChequeRow] = processResultSetList(cs, 3, processPaymentAllocationChequeRow, context)

      PaymentAllocationChequeDetails(
        paymentAllocationDetails = paymentAllocationChequeRow
      )

    } finally {
      cs.close()
    }
  }

  private def processPaymentAllocationChequeRow(rs: ResultSet): PaymentAllocationChequeRow = {
    PaymentAllocationChequeRow(
      accountingPeriod       = rs.getLong("ACCOUNTING_PERIOD"), // ACCOUNTING_PERIOD
      apEndDate              = rs.getDate("AP_END_DATE").toLocalDate, // AP_END_DATE
      amount                 = rs.getBigDecimal("AMOUNT"), // AMOUNT
      effectiveDateOfPayment = rs.getDate("EFFECTIVE_DATE_OF_PAYMENT").toLocalDate // EFFECTIVE_DATE_OF_PAYMENT
    )

  }
}
