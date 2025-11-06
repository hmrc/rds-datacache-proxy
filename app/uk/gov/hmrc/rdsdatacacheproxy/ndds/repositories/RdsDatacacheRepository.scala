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

package uk.gov.hmrc.rdsdatacacheproxy.ndds.repositories

import oracle.jdbc.OracleTypes
import play.api.{Logging, db}
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ndds.config.AppConfig
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.requests.PaymentPlanDuplicateCheckRequest
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses.*

import java.sql.{Date, ResultSet, Types}
import java.time.LocalDate
import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

trait RdsDataSource {
  def getDirectDebits(id: String): Future[UserDebits]
  def addFutureWorkingDays(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate]
  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference]
  def getDirectDebitPaymentPlans(directDebitReference: String, credId: String): Future[DDPaymentPlans]
  def getPaymentPlanDetails(directDebitReference: String, credId: String, paymentPlanReference: String): Future[PaymentPlanDetails]
  def lockPaymentPlan(paymentPlanReference: String, credId: String): Future[PaymentPlanLock]
  def isDuplicatePaymentPlan(directDebitReference: String, credId: String, request: PaymentPlanDuplicateCheckRequest): Future[DuplicateCheckResponse]
  def isAdvanceNoticePresent(paymentPlanReference: String, credId: String): Future[AdvanceNoticeResponse]
}

class RdsDatacacheRepository @Inject() (db: Database, appConfig: AppConfig)(implicit ec: ExecutionContext) extends RdsDataSource with Logging:

  def getDirectDebits(id: String): Future[UserDebits] = {
    val pFirstRecord = appConfig.firstRecord
    val pMaxRecords = appConfig.maxRecords
    logger.info(s"Input request Credential ID: $id, firstRecordIndex: $pFirstRecord, maxRecords: $pMaxRecords")

    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.getDDSummary(?, ?, ?, ?, ?, ?)}")

        // Set input parameters
        storedProcedure.setString("pCredentialID", id) // pCredentialID
        storedProcedure.setInt("pFirstRecordNumber", pFirstRecord) // pFirstRecordNumber
        storedProcedure.setInt("pMaxRecords", pMaxRecords) // pMaxRecords

        // Register output parameters
        storedProcedure.registerOutParameter("pTotalRecords", Types.NUMERIC) // pTotalRecords
        storedProcedure.registerOutParameter("pDDSummary", OracleTypes.CURSOR) // pDDSummary
        storedProcedure.registerOutParameter("pResponseStatus", Types.VARCHAR) // pResponseStatus

        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters
        val debitTotal = storedProcedure.getInt("pTotalRecords") // pTotalRecords
        val directDebitSet = storedProcedure.getObject("pDDSummary", classOf[ResultSet]) // pDDSummary (REF CURSOR)
        val responseStatus = storedProcedure.getString("pResponseStatus") // pResponseStatus
        logger.info(s"DD count from SQL stored procedure: $debitTotal")
        logger.info(s"DB Response status from SQL stored procedure: $responseStatus")

        def collectDirectDebits(rs: java.sql.ResultSet): List[DirectDebit] = {
          Iterator
            .continually(rs.next())
            .takeWhile(identity)
            .map(_ =>
              DirectDebit(
                ddiRefNumber       = rs.getString("DDIRefNumber"),
                submissionDateTime = rs.getTimestamp("SubmissionDateTime").toLocalDateTime,
                bankSortCode       = rs.getString("BankSortCode"),
                bankAccountNumber  = rs.getString("BankAccountNumber"),
                bankAccountName    = if (rs.getString("BankAccountName") == null) "" else rs.getString("BankAccountName"),
                auDdisFlag         = rs.getBoolean("AuddisFlag"),
                numberOfPayPlans   = rs.getInt("NumberofPayPlans")
              )
            )
            .toList
        }

        val directDebits = collectDirectDebits(directDebitSet)
        directDebitSet.close()
        storedProcedure.close()
        connection.close()

        // Return UserDebits
        UserDebits(debitTotal, directDebits)
      }
    }
  }

  def addFutureWorkingDays(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] = {
    logger.info(s"Input request payment date. Base date: <${Date.valueOf(baseDate)}>, Working days offset: <$offsetWorkingDays>")
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.AddWorkingDays(?, ?, ?)}")

        storedProcedure.setDate("pInputDate", Date.valueOf(baseDate))
        storedProcedure.setInt("pNumberofWorkingDays", offsetWorkingDays)

        storedProcedure.registerOutParameter("pOutputDate", Types.DATE)
        storedProcedure.execute()

        val outputDate = storedProcedure.getDate("pOutputDate")

        storedProcedure.close()
        connection.close()

        logger.info(s"Future payment date from SQL Stored Procedure: $outputDate")
        EarliestPaymentDate(outputDate.toLocalDate)
      }
    }
  }

  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference] = {
    logger.info(s"Input request, pPayReference: <$paymentReference>, pCredentialID: <$credId>, pSessionID: <$sessionId>")
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.GETDDIRefNumber(?, ?, ?, ?)}")

        storedProcedure.setString("pPayReference", paymentReference)
        storedProcedure.setString("pCredentialID", credId)
        storedProcedure.setString("pSessionID", sessionId)

        storedProcedure.registerOutParameter("pDDIRefNumber", Types.VARCHAR)
        storedProcedure.execute()

        val ddiRef = storedProcedure.getString("pDDIRefNumber")

        storedProcedure.close()
        connection.close()

        logger.info(s"DDI reference number from SQL Stored Procedure: $ddiRef")
        DDIReference(ddiRef)
      }
    }
  }

  def getDirectDebitPaymentPlans(directDebitReference: String, credId: String): Future[DDPaymentPlans] = {
    val pFirstRecord = appConfig.firstRecord
    val pMaxRecords = appConfig.maxRecords
    logger.info(
      s"**** Cred ID: $credId, Direct Debit Reference: $directDebitReference " +
        s"FirstRecordNumber: $pFirstRecord, Max Records: $pMaxRecords"
    )

    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.getPayPlanSummary(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

        // Set input parameters
        storedProcedure.setString("pCredentialID", credId) // pCredentialID
        storedProcedure.setString("pDDIRefNumber", directDebitReference) // pDDIRefNumber
        storedProcedure.setInt("pFirstRecordNumber", pFirstRecord) // pFirstRecordNumber
        storedProcedure.setInt("pMaxRecords", pMaxRecords) // pMaxRecords

        // Register output parameters
        storedProcedure.registerOutParameter("pBankSortCode", Types.VARCHAR) // pBankSortCode
        storedProcedure.registerOutParameter("pBankAccountNumber", Types.VARCHAR) // pBankAccountNumber
        storedProcedure.registerOutParameter("pBankAccountName", Types.VARCHAR) // pBankAccountName
        storedProcedure.registerOutParameter("pAUDDISFlag", Types.VARCHAR) // pAUDDISFlag
        storedProcedure.registerOutParameter("pTotalRecords", Types.NUMERIC) // pTotalRecords
        storedProcedure.registerOutParameter("pPayPlanSummary", OracleTypes.CURSOR) // pPayPlanSummary
        storedProcedure.registerOutParameter("pResponseStatus", Types.VARCHAR) // pResponseStatus

        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters
        val sortCode = storedProcedure.getString("pBankSortCode") // pBankSortCode
        val bankAccountNumber = storedProcedure.getString("pBankAccountNumber") // pBankAccountNumber
        val bankAccountName =
          if (storedProcedure.getString("pBankAccountName") == null) "" else storedProcedure.getString("pBankAccountName") // pBankAccountName
        val auDdisFlag = storedProcedure.getString("pAUDDISFlag") // pAUDDISFlag
        val paymentPlansCount = storedProcedure.getInt("pTotalRecords") // pTotalRecords
        val paymentPlans = storedProcedure.getObject("pPayPlanSummary", classOf[ResultSet]) // pPayPlanSummary (REF CURSOR)
        val responseStatus = storedProcedure.getString("pResponseStatus") // pResponseStatus
        logger.info(s"***** Payment plans count: $paymentPlansCount")
        logger.info(s"DB Response status: $responseStatus")

        // Tail-recursive function to collect payment plans
        @tailrec
        def collectPaymentPlans(pp: List[PaymentPlan] = Nil): List[PaymentPlan] = {
          if (!paymentPlans.next()) pp.reverse
          else {
            val paymentPlan = PaymentPlan(
              scheduledPaymentAmount = paymentPlans.getBigDecimal("ScheduledPayAmount"),
              planRefNumber          = paymentPlans.getString("PPRefNumber"),
              planType               = paymentPlans.getString("PayPlanType"),
              paymentReference       = paymentPlans.getString("PayReference"),
              hodService             = paymentPlans.getString("PayPlanHodService"),
              submissionDateTime     = paymentPlans.getTimestamp("SubmissionDateTime").toLocalDateTime
            )
            collectPaymentPlans(paymentPlan :: pp)
          }
        }

        val paymentPlanList = collectPaymentPlans()

        paymentPlans.close()
        storedProcedure.close()
        connection.close()
        // Return DDPaymentPlans
        DDPaymentPlans(sortCode, bankAccountNumber, bankAccountName, auDdisFlag, paymentPlansCount, paymentPlanList)
      }
    }
  }

  def getPaymentPlanDetails(directDebitReference: String, credId: String, paymentPlanReference: String): Future[PaymentPlanDetails] = {

    logger.info(
      s"**** Cred ID: $credId, Direct Debit Reference: $directDebitReference " +
        s"Payment Plan Reference: $paymentPlanReference"
    )

    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall(
          "{call DD_PK.getPayPlanDetails(?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?)}"
        )

        // Set input parameters
        storedProcedure.setString("pCredentialID", credId) // pCredentialID
        storedProcedure.setString("pDDIRefNumber", directDebitReference) // pDDIRefNumber
        storedProcedure.setString("pPPRefNumber", paymentPlanReference) // pPPRefNumber

        // Register output parameters
        storedProcedure.registerOutParameter("pBankAccountNumber", Types.VARCHAR) // pBankAccountNumber
        storedProcedure.registerOutParameter("pBankSortCode", Types.VARCHAR) // pBankSortCode
        storedProcedure.registerOutParameter("pBankAccountName", Types.VARCHAR) // pBankAccountName
        storedProcedure.registerOutParameter("pDDISubmissionDateTime", Types.TIMESTAMP) // pDDISubmissionDateTime
        storedProcedure.registerOutParameter("pAUDDISFlag", Types.VARCHAR) // pAUDDISFlag
        storedProcedure.registerOutParameter("pPayPlanHodService", Types.VARCHAR) // pPayPlanHodService
        storedProcedure.registerOutParameter("pPayPlanType", Types.VARCHAR) // pPayPlanType
        storedProcedure.registerOutParameter("pPayReference", Types.VARCHAR) // pPayReference
        storedProcedure.registerOutParameter("pSubmissionDateTime", Types.TIMESTAMP) // pSubmissionDateTime
        storedProcedure.registerOutParameter("pScheduledPayAmount", Types.DECIMAL) // pScheduledPayAmount
        storedProcedure.registerOutParameter("pScheduledPayStartDate", Types.DATE) // pScheduledPayStartDate
        storedProcedure.registerOutParameter("pInitialPayStartDate", Types.DATE) // pInitialPayStartDate
        storedProcedure.registerOutParameter("pInitialPayAmount", Types.DECIMAL) // pInitialPayAmount
        storedProcedure.registerOutParameter("pScheduledPayEndDate", Types.DATE) // pScheduledPayEndDate
        storedProcedure.registerOutParameter("pScheduledPayFreq", Types.NUMERIC) // pScheduledPayFreq
        storedProcedure.registerOutParameter("pSuspensionStartDate", Types.DATE) // pSuspensionStartDate
        storedProcedure.registerOutParameter("pSuspensionEndDate", Types.DATE) // pSuspensionEndDate
        storedProcedure.registerOutParameter("pBalancingPayAmount", Types.DECIMAL) // pBalancingPayAmount
        storedProcedure.registerOutParameter("pBalancingPayDate", Types.DATE) // pBalancingPayDate
        storedProcedure.registerOutParameter("pTotalLiability", Types.DECIMAL) // pTotalLiability
        storedProcedure.registerOutParameter("pPayPlanEditFlag", Types.NUMERIC) // pPayPlanEditFlag
        storedProcedure.registerOutParameter("pResponseStatus", Types.VARCHAR) // pResponseStatus

        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters

        // Direct debit details
        val bankAccountNumber = storedProcedure.getString("pBankAccountNumber") // pBankAccountNumber
        val sortCode = storedProcedure.getString("pBankSortCode") // pBankSortCode
        val bankAccountName = storedProcedure.getString("pBankAccountName") // pBankAccountName
        val ddSubmissionDateTime = storedProcedure.getTimestamp("pDDISubmissionDateTime") // pDDISubmissionDateTime
        val auDdisFlag = storedProcedure.getString("pAUDDISFlag") // pAUDDISFlag

        // Payment plan details
        val hodService = storedProcedure.getString("pPayPlanHodService") // pPayPlanHodService
        val planType = storedProcedure.getString("pPayPlanType") // pPayPlanType
        val paymentReference = storedProcedure.getString("pPayReference") // pPayReference
        val paymentPlanSubmissionDateTime = storedProcedure.getTimestamp("pSubmissionDateTime") // pSubmissionDateTime
        val scheduledPaymentAmount = storedProcedure.getBigDecimal("pScheduledPayAmount") // pScheduledPayAmount
        val scheduledPaymentStartDate = storedProcedure.getDate("pScheduledPayStartDate") // pScheduledPayStartDate
        val initialPaymentStartDate = storedProcedure.getDate("pInitialPayStartDate") // pInitialPayStartDate
        val initialPaymentAmount = storedProcedure.getBigDecimal("pInitialPayAmount") // pInitialPayAmount
        val scheduledPaymentEndDate = storedProcedure.getDate("pScheduledPayEndDate") // pScheduledPayEndDate
        val scheduledPaymentFrequency = storedProcedure.getInt("pScheduledPayFreq") // pScheduledPayFreq
        val suspensionStartDate = storedProcedure.getDate("pSuspensionStartDate") // pSuspensionStartDate
        val suspensionEndDate = storedProcedure.getDate("pSuspensionEndDate") // pSuspensionEndDate
        val balancingPaymentAmount = storedProcedure.getBigDecimal("pBalancingPayAmount") // pBalancingPayAmount
        val balancingPaymentDate = storedProcedure.getDate("pBalancingPayDate") // pBalancingPayDate
        val totalLiability = storedProcedure.getBigDecimal("pTotalLiability") // pTotalLiability
        val paymentPlanEditable = storedProcedure.getInt("pPayPlanEditFlag") // PayPlanEditFlag
        val responseStatus = storedProcedure.getString("pResponseStatus") // pResponseStatus

        logger.info(s"DB Response status: $responseStatus")

        storedProcedure.close()
        connection.close()

        // Return PaymentPlanDetails
        PaymentPlanDetails(
          directDebitDetails = DirectDebitDetail(
            bankSortCode       = Option(sortCode),
            bankAccountNumber  = Option(bankAccountNumber),
            bankAccountName    = Option(bankAccountName),
            auDdisFlag         = auDdisFlag == "1",
            submissionDateTime = ddSubmissionDateTime.toLocalDateTime
          ),
          paymentPlanDetails = PaymentPlanDetail(
            hodService                = hodService,
            planType                  = planType,
            paymentReference          = paymentReference,
            submissionDateTime        = paymentPlanSubmissionDateTime.toLocalDateTime,
            scheduledPaymentAmount    = Option(scheduledPaymentAmount),
            scheduledPaymentStartDate = Option(scheduledPaymentStartDate).map(_.toLocalDate),
            initialPaymentStartDate   = Option(initialPaymentStartDate).map(_.toLocalDate),
            initialPaymentAmount      = Option(initialPaymentAmount),
            scheduledPaymentEndDate   = Option(scheduledPaymentEndDate).map(_.toLocalDate),
            scheduledPaymentFrequency = Option(scheduledPaymentFrequency),
            suspensionStartDate       = Option(suspensionStartDate).map(_.toLocalDate),
            suspensionEndDate         = Option(suspensionEndDate).map(_.toLocalDate),
            balancingPaymentAmount    = Option(balancingPaymentAmount),
            balancingPaymentDate      = Option(balancingPaymentDate).map(_.toLocalDate),
            totalLiability            = Option(totalLiability),
            paymentPlanEditable       = paymentPlanEditable == 1
          )
        )
      }
    }
  }

  def lockPaymentPlan(paymentPlanReference: String, credId: String): Future[PaymentPlanLock] = {
    logger.info(s"**** Cred ID: $credId, Payment Plan Reference: $paymentPlanReference")

    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall(
          "{call DD_PK.LockPayPlan(?, ?, ?)}"
        )

        // Set input parameters
        storedProcedure.setString("pCredentialID", credId) // pCredentialID
        storedProcedure.setString("pPPRefNumber", paymentPlanReference) // pPPRefNumber

        // Register output parameters
        storedProcedure.registerOutParameter("pResponseStatus", Types.VARCHAR) // pResponseStatus
        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters
        val responseStatus = storedProcedure.getString("pResponseStatus") // pResponseStatus
        logger.info(s"DB Response status: $responseStatus")

        storedProcedure.close()
        connection.close()

        // Return PaymentPlanLock
        PaymentPlanLock(lockSuccessful = responseStatus.equalsIgnoreCase("PP FOUND"))
      }
    }
  }

  def isDuplicatePaymentPlan(directDebitReference: String,
                             credId: String,
                             request: PaymentPlanDuplicateCheckRequest
                            ): Future[DuplicateCheckResponse] = {
    logger.info(s"**** Direct Debit Reference: $directDebitReference")

    Future {
      db.withConnection { connection =>

        val storedProcedure = connection.prepareCall("{call DD_PK.isDuplicatePaymentPlan(?, ?, ?, ?, ?, ?, ?, ?,?,?)}")

        // Set input parameters
        storedProcedure.setString("pCredentialID", credId) // pCredentialID
        storedProcedure.setString("pPPRefNumber", request.paymentPlanReference) // pPPRefNumber
        storedProcedure.setString("pPayPlanType", request.planType) // pPayPlanType
        storedProcedure.setString("pPayPlanHodService", request.paymentService) // pPayPlanHodService
        storedProcedure.setString("pPayReference", request.paymentReference) // pPayReference
        request.paymentAmount match { // pScheduledPayAmount
          case Some(paymentAmount) => storedProcedure.setBigDecimal("pScheduledPayAmount", paymentAmount.bigDecimal)
          case None                => storedProcedure.setNull("pScheduledPayAmount", java.sql.Types.INTEGER)
        }
        request.totalLiability match { // pTotalLiability
          case Some(totalLiabilityAmount) => storedProcedure.setBigDecimal("pTotalLiability", totalLiabilityAmount.bigDecimal)
          case None                       => storedProcedure.setNull("pTotalLiability", java.sql.Types.INTEGER)
        }
        request.paymentFrequency match { // pScheduledPayFreq
          case Some(freq) => storedProcedure.setInt("pScheduledPayFreq", freq)
          case None       => storedProcedure.setNull("pScheduledPayFreq", java.sql.Types.INTEGER)
        }
        storedProcedure.setDate("pScheduledPayStartDate", Date.valueOf(request.paymentStartDate)) // pScheduledPayStartDate

        // Register output parameters
        storedProcedure.registerOutParameter("pDuplicatePayPlan", Types.NUMERIC) // pDuplicatePayPlan

        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters
        val isDuplicate = storedProcedure.getInt("pDuplicatePayPlan") // pDuplicatePayPlan

        storedProcedure.close()
        connection.close()

        // Return DuplicateCheckResponse
        DuplicateCheckResponse(isDuplicate == 1)
      }
    }
  }

  def isAdvanceNoticePresent(paymentPlanReference: String, credId: String): Future[AdvanceNoticeResponse] = {
    logger.info(s"**** Cred ID: $credId, Payment Plan Reference: $paymentPlanReference")

    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall(
          "{call RTI_ENDDS_PCK.get_advance_notice(?, ?, ?)}"
        )

        // Set input parameters
        storedProcedure.setString("p_payment_plan_reference", paymentPlanReference) // p_payment_plan_reference

        // Register output parameters
        storedProcedure.registerOutParameter("p_total_amount", Types.NUMERIC) // p_total_amount
        storedProcedure.registerOutParameter("p_due_date", java.sql.Types.DATE) // p_due_date
        // Execute the stored procedure
        storedProcedure.execute()

        // Retrieve output parameters
        val responseTotalAmount = storedProcedure.getString("p_total_amount") // p_total_amount
        val responseDueDate = storedProcedure.getString("p_due_date") // p_due_date
        logger.info(s"DB Response for total amount : $responseTotalAmount and due date: $responseDueDate")

        storedProcedure.close()
        connection.close()

        // Return AdvanceNoticeResponse
        AdvanceNoticeResponse(
          Option(storedProcedure.getBigDecimal("p_total_amount")),
          Option(storedProcedure.getDate("p_due_date")).map(_.toLocalDate)
        )

      }
    }
  }
