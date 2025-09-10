/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.repositories

import oracle.jdbc.OracleTypes
import play.api.{Logging, db}
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDIReference, DDPaymentPlans, DirectDebit, EarliestPaymentDate, PaymentPlan, UserDebits}

import java.sql.{Date, ResultSet, Types}
import java.time.LocalDate
import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

trait RdsDataSource {
  def getDirectDebits(id: String, start: Int, max: Int): Future[UserDebits]
  def getEarliestPaymentDate(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate]
  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference]
  def getDirectDebitPaymentPlans(paymentReference: String, credId: String, start: Int, max: Int): Future[DDPaymentPlans]
}

class RdsDatacacheRepository @Inject()(db: Database)(implicit ec: ExecutionContext) extends RdsDataSource with Logging:

  def getDirectDebits(id: String, start: Int, max: Int): Future[UserDebits] = {
    logger.info(s"**** Cred ID: ${id}, FirstRecordNumber: ${start}, Max Records: ${max}")

    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.getDDSummary(?, ?, ?, ?, ?, ?)}")

        // Set input parameters
        storedProcedure.setString("pCredentialID", id) // pCredentialID
        storedProcedure.setInt("pFirstRecordNumber", start) // pFirstRecordNumber
        storedProcedure.setInt("pMaxRecords", max) // pMaxRecords

        // Register output parameters
        storedProcedure.registerOutParameter("pTotalRecords", Types.NUMERIC) // pTotalRecords
        storedProcedure.registerOutParameter("pDDSummary", OracleTypes.CURSOR) // pDDSummary
        storedProcedure.registerOutParameter("pResponseStatus", Types.VARCHAR) // pResponseStatus

        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters
        val debitTotal = storedProcedure.getInt("pTotalRecords") // pTotalRecords
        val debits = storedProcedure.getObject("pDDSummary", classOf[ResultSet]) // pDDSummary (REF CURSOR)
        val responseStatus = storedProcedure.getString("pResponseStatus") // pResponseStatus

        // Tail-recursive function to collect debits
        @tailrec
        def collectDebits(acc: Seq[DirectDebit] = Seq.empty): Seq[DirectDebit] = {
          if (!debits.next()) acc
          else {
            val directDebit = DirectDebit(
              ddiRefNumber = debits.getString("DDIRefNumber"),
              submissionDateTime = debits.getTimestamp("SubmissionDateTime").toLocalDateTime,
              bankSortCode = debits.getString("BankSortCode"),
              bankAccountNumber = debits.getString("BankAccountNumber"),
              bankAccountName = debits.getString("BankAccountName"),
              auDdisFlag = debits.getBoolean("AuddisFlag"),
              numberOfPayPlans = debits.getInt("NumberofPayPlans")
            )
            collectDebits(acc :+ directDebit)
          }
        }

        val result = collectDebits()
        logger.info(s"***** DD count: $debitTotal")
        logger.info(s"DB Response status: $responseStatus")

        storedProcedure.close()

        // Return UserDebits
        UserDebits(debitTotal, result)
      }
    }
  }

  def getEarliestPaymentDate(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] = {
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.AddWorkingDays(?, ?, ?)}")

        storedProcedure.setDate("pInputDate", Date.valueOf(baseDate))
        storedProcedure.setInt("pNumberofWorkingDays", offsetWorkingDays)

        logger.info(s"Getting earliest payment date. Base date: <${Date.valueOf(baseDate)}>, Working days offset: <$offsetWorkingDays>")

        storedProcedure.registerOutParameter("pOutputDate", Types.DATE)
        storedProcedure.execute()

        val outputDate = storedProcedure.getDate("pOutputDate")

        storedProcedure.close()

        logger.info(s"Getting earliest payment date. Result from SQL Stored Procedure: $outputDate")
        EarliestPaymentDate(outputDate.toLocalDate)
      }
    }
  }

  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference] = {
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.GETDDIRefNumber(?, ?, ?, ?)}")

        storedProcedure.setString("pPayReference", paymentReference)
        storedProcedure.setString("pCredentialID", credId)
        storedProcedure.setString("pSessionID", sessionId)

        logger.info(s"Getting DDI Ref, pPayReference: <${paymentReference}>, pCredentialID: <$credId>, pSessionID: <$sessionId>")

        storedProcedure.registerOutParameter("pDDIRefNumber", Types.VARCHAR)
        storedProcedure.execute()

        val ddiRef = storedProcedure.getString("pDDIRefNumber")

        storedProcedure.close()

        logger.info(s"Getting DDI Ref, Result from SQL Stored Procedure: $ddiRef")
        DDIReference(ddiRef)
      }
    }
  }

  def getDirectDebitPaymentPlans(paymentReference: String, credId: String, start: Int, max: Int):
  Future[DDPaymentPlans] = {
    logger.info(s"**** Cred ID: ${credId}, Payment Reference: ${paymentReference} " +
      s"FirstRecordNumber: ${start}, Max Records: ${max}")

    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.getPayPlanSummary(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

        // Set input parameters
        storedProcedure.setString("pCredentialID", credId) // pCredentialID
        storedProcedure.setString("pDDIRefNumber", paymentReference) // pDDIRefNumber
        storedProcedure.setInt("pFirstRecordNumber", start) // pFirstRecordNumber
        storedProcedure.setInt("pMaxRecords", max) // pMaxRecords

        // Register output parameters
        storedProcedure.registerOutParameter("pBankSortCode", Types.VARCHAR) // pBankSortCode
        storedProcedure.registerOutParameter("pBankAccountNumber", Types.VARCHAR) // pBankAccountNumber
        storedProcedure.registerOutParameter("pBankAccountName", Types.VARCHAR) // pBankAccountName
        storedProcedure.registerOutParameter("pTotalRecords", Types.NUMERIC) // pTotalRecords
        storedProcedure.registerOutParameter("pDDSummary", OracleTypes.CURSOR) // pDDSummary
        storedProcedure.registerOutParameter("pResponseStatus", Types.VARCHAR) // pResponseStatus

        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters
        val sortCode = storedProcedure.getString("pBankSortCode") // pBankSortCode
        val bankAccountNumber = storedProcedure.getString("pBankAccountNumber") // pBankAccountNumber
        val bankAccountName = storedProcedure.getString("pBankAccountName") // pBankAccountName
        val paymentPlansCount = storedProcedure.getInt("pTotalRecords") // pTotalRecords
        val paymentPlans = storedProcedure.getObject("pPayPlanSummary", classOf[ResultSet]) // pPayPlanSummary (REF CURSOR)
        val responseStatus = storedProcedure.getString("pResponseStatus") // pResponseStatus

        // Tail-recursive function to collect payment plans
        @tailrec
        def collectPaymentPlans(pp: Seq[PaymentPlan] = Seq.empty): Seq[PaymentPlan] = {
          if (!paymentPlans.next()) pp
          else {
            val paymentPlan = PaymentPlan(
              scheduledPayAmount = paymentPlans.getDouble("pScheduledPayAmount"),
              planType = paymentPlans.getString("pPayPlanType"),
              payReference = paymentPlans.getString("pPayReference"),
              planHoldService = paymentPlans.getString("pPayPlanHodService"),
              submissionDateTime = paymentPlans.getTimestamp("SubmissionDateTime").toLocalDateTime,
            )
            collectPaymentPlans(pp :+ paymentPlan)
          }
        }

        val result = collectPaymentPlans()
        logger.info(s"***** Payment plans count: $paymentPlansCount")
        logger.info(s"DB Response status: $responseStatus")

        storedProcedure.close()

        // Return DDPaymentPlans
        DDPaymentPlans(sortCode, bankAccountNumber, bankAccountName, paymentPlansCount, result)
      }
    }
  }
