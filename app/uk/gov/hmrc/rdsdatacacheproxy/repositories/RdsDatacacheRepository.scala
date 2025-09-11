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
import uk.gov.hmrc.rdsdatacacheproxy.config.AppConfig
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDIReference, DirectDebit, EarliestPaymentDate, UserDebits}

import java.sql.{Date, ResultSet, Types}
import java.time.LocalDate
import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

trait RdsDataSource {
  def getDirectDebits(id: String): Future[UserDebits]
  def addFutureWorkingDays(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate]
  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference]
}

class RdsDatacacheRepository @Inject()(db: Database, appConfig: AppConfig)(implicit ec: ExecutionContext) extends RdsDataSource with Logging:

  def getDirectDebits(id: String): Future[UserDebits] = {
    logger.info(s"Input request Credential ID: $id")
    val pFirstRecord = appConfig.firstRecord
    val pMaxRecords = appConfig.maxRecords

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
        logger.info(s"DD count from SQL stored procedure: $debitTotal")
        logger.info(s"DB Response status from SQL stored procedure: $responseStatus")
        logger.info(s"Direct Debits from Stored Procedure: $result")

        storedProcedure.close()

        // Return UserDebits
        UserDebits(debitTotal, result)
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

        logger.info(s"Future payment date from SQL Stored Procedure: $outputDate")
        EarliestPaymentDate(outputDate.toLocalDate)
      }
    }
  }

  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference] = {
    logger.info(s"Input request, pPayReference: <${paymentReference}>, pCredentialID: <$credId>, pSessionID: <$sessionId>")
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

        logger.info(s"DDI reference number from SQL Stored Procedure: $ddiRef")
        DDIReference(ddiRef)
      }
    }
  }
