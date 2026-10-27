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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{ElectronicPaymentAllocationDetails, ElectronicPaymentAllocationInfo}

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ElectronicPaymentAllocationDetailsRepositoryImpl])
trait ElectronicPaymentAllocationDetailsRepository {
  def getPayAllocationElectronicDtls(taxRef: Long, accPeriod: Long, payslipId: Long): Future[ElectronicPaymentAllocationDetails]
}

class ElectronicPaymentAllocationDetailsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends ElectronicPaymentAllocationDetailsRepository
    with Logging {

  def getPayAllocationElectronicDtls(taxRef: Long, accPeriod: Long, payslipId: Long): Future[ElectronicPaymentAllocationDetails] = {
    Future {
      db.withConnection { connect =>
        val sp = connect.prepareCall("{call CT_DC_PK.getPayAllocationElectronicDtls(?, ?, ?, ?)}")

        try {
          sp.setLong(1, taxRef)
          sp.setLong(2, accPeriod)
          sp.setLong(3, payslipId)
          sp.registerOutParameter(4, OracleTypes.CURSOR)

          sp.execute()

          val ePaymentAllocationInfo = sp.getObject(4, classOf[ResultSet])

          val ePaymentAllocationDetails = readEPaymentAllocationDetails(ePaymentAllocationInfo)

          ElectronicPaymentAllocationDetails(
            electronicPaymentAllocationDetails = ePaymentAllocationDetails
          )

        } finally {
          sp.close()
        }
      }
    }
  }

  private def readEPaymentAllocationDetails(rs: ResultSet): List[ElectronicPaymentAllocationInfo] = {
    val buffer = ListBuffer[ElectronicPaymentAllocationInfo]()
    while (rs.next()) {
      buffer += ElectronicPaymentAllocationInfo(
        accountingPeriod       = rs.getBigDecimal("accounting_period"),
        apEndDate              = rs.getDate("ap_end_date").toLocalDate,
        amount                 = rs.getBigDecimal("amount"),
        effectiveDateOfPayment = rs.getDate("effective_date_of_payment").toLocalDate
      )
    }
    buffer.toList
  }
}
