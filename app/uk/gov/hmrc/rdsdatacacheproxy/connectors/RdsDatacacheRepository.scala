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

package uk.gov.hmrc.rdsdatacacheproxy.connectors

import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.EarliestPaymentDate
import uk.gov.hmrc.rdsdatacacheproxy.models.{DirectDebit, UserDebits}

import java.sql.{Date, ResultSet, Types}
import java.time.LocalDate
import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

trait RdsDataSource {
  def getDirectDebits(id: String, start: Int, max: Int): Future[UserDebits]
  def getEarliestPaymentDate(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate]
}

class RdsDatacacheRepository @Inject()(db: Database)(implicit ec: ExecutionContext) extends RdsDataSource with Logging:

  def getDirectDebits(id: String, start: Int, max: Int): Future[UserDebits] = {
    logger.info(s"**** Cred ID: ${id}, FirstRecordNumber: ${start}, Max Records: ${max}")

    Future {
      db.withConnection { connection =>
        logger.info(s"DB connection successful...${connection}")

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

        logger.info(s"DB response: ${debits}")
        logger.info(s"DB Response status: $responseStatus")

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
        logger.info(s"***** DD count: $debitTotal, DD details: ${result}")

        storedProcedure.close()

        // Return UserDebits
        UserDebits(debitTotal, result)
      }
    }
  }

  def getEarliestPaymentDate(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] =
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call DD_PK.AddWorkingDays(?, ?, ?)}")

        storedProcedure.setDate("pInputDate", Date(baseDate.toEpochDay))
        storedProcedure.setInt("pNumberofWorkingDays", offsetWorkingDays)

        logger.info(s"Getting earliest payment date. Base date: <$baseDate>, Working days offset: <$offsetWorkingDays>")

        storedProcedure.registerOutParameter("pOutputDate", Types.DATE)
        storedProcedure.execute()

        val date = storedProcedure.getDate("pOutputDate")

        storedProcedure.close()

        logger.info(s"Getting earliest payment date. Result from SQL Stored Procedure: $date")
        EarliestPaymentDate(date.toLocalDate)
      }
    }
