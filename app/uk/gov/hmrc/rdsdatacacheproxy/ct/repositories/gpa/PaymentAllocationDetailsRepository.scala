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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.{AllocationDetails, PaymentAllocationDetails}

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PaymentAllocationDetailsRepositoryImpl])
trait PaymentAllocationDetailsRepository {
  def getGPAPaymentAllocationDetail(gpaUtr: Long,
                                    gppContractVersion: Long,
                                    participatorUtr: Long,
                                    participatorAp: Long,
                                    startIndex: Long,
                                    count: Long
                                   ): Future[PaymentAllocationDetails]
}

class PaymentAllocationDetailsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends PaymentAllocationDetailsRepository
    with Logging {

  def getGPAPaymentAllocationDetail(gpaUtr: Long,
                                    gppContractVersion: Long,
                                    participatorUtr: Long,
                                    participatorAp: Long,
                                    startIndex: Long,
                                    count: Long
                                   ): Future[PaymentAllocationDetails] = {
    Future {
      db.withConnection { connect =>
        val sp = connect.prepareCall("{call CT_GPA_PK.getGPAPaymentAllocationDetail(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

        try {
          sp.setLong(1, gpaUtr)
          sp.setLong(2, gppContractVersion)
          sp.setLong(3, participatorUtr)
          sp.setLong(4, participatorAp)
          sp.setLong(5, startIndex)
          sp.setLong(6, count)

          // getGPAPaymentAllocOverview
          sp.registerOutParameter(7, OracleTypes.DATE) // pGPP_END_DATE
          sp.registerOutParameter(8, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_PAYMENT
          sp.registerOutParameter(9, OracleTypes.NUMERIC) // pGPP_TOTAL_GROUP_TAX
          sp.registerOutParameter(10, OracleTypes.VARCHAR) // pGPP_STATUS
          sp.registerOutParameter(11, OracleTypes.CHAR) // pGPP_APPORTIONMENT_METHOD
          sp.registerOutParameter(12, OracleTypes.VARCHAR) // pPARTICIP_COMPANY_DESC
          sp.registerOutParameter(13, OracleTypes.DATE) // pPARTICIP_ACC_PERIOD_ENDING
          sp.registerOutParameter(14, OracleTypes.NUMERIC) // pPARTICIP_TAX_CHARGE
          sp.registerOutParameter(15, OracleTypes.NUMERIC) // pPARTICIP_ALLOCATED_PAYMENTS
          sp.registerOutParameter(16, OracleTypes.NUMERIC) // pGPA_UTR_OUT
          sp.registerOutParameter(17, OracleTypes.NUMERIC) // pGPP_CONTRACT_VERSION_OUT

          // getGPAPaymentAllocations
          sp.registerOutParameter(18, OracleTypes.REF_CURSOR) // pPAYMENTS
          sp.registerOutParameter(19, OracleTypes.NUMERIC) // pTOTAL_NUM_OF_RECORDS

          sp.execute()

          val allocationDetails = sp.getObject(18, classOf[ResultSet])

          val allocationsList = readAllocationDetails(allocationDetails)

          PaymentAllocationDetails(
            gppEndDate                = sp.getDate(7).toLocalDate,
            gppTotalGroupPayment      = Option(sp.getBigDecimal(8)),
            gppTotalGroupTax          = Option(sp.getBigDecimal(9)),
            gppStatus                 = sp.getString(10),
            gppApportionmentMethod    = Option(sp.getString(11)),
            participatingCompanyDesc  = sp.getString(12),
            participatorAccPeriodEnd  = sp.getDate(13).toLocalDate,
            participatorTaxCharge     = sp.getBigDecimal(14),
            participatorAllocPayments = sp.getBigDecimal(15),
            gpaUtr                    = sp.getBigDecimal(16),
            gppContractVersionOut     = sp.getBigDecimal(17),
            allocationDetails         = allocationsList,
            totalNumOfRecords         = sp.getBigDecimal(19)
          )

        } finally {
          sp.close()
        }
      }
    }
  }

  private def readAllocationDetails(rs: ResultSet): List[AllocationDetails] = {
    val buffer = ListBuffer[AllocationDetails]()
    while (rs.next()) {
      buffer += AllocationDetails(
        effectivePaymentDate = rs.getDate("effective_payment_date").toLocalDate,
        paymentAmount        = rs.getBigDecimal("payment_amount")
      )
    }
    buffer.toList
  }
}
