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

class RdsDatacacheRepository @Inject()(db: Database)(implicit ec: ExecutionContext) extends RdsDataSource:

  def getDirectDebits(id: String, start: Int, max: Int): Future[UserDebits] =
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("DD_PK.getDDSummary")

        storedProcedure.setString("pCredentialID", id)
        storedProcedure.setInt("pFirstRecordNumber", start)
        storedProcedure.setInt("pMaxRecords", max)

        storedProcedure.registerOutParameter("pTotalRecords", Types.NUMERIC)
        storedProcedure.registerOutParameter("pDDSummary", Types.REF_CURSOR)
        storedProcedure.registerOutParameter("pResponseStatus", Types.VARCHAR)

        storedProcedure.execute()

        val debitTotal = storedProcedure.getInt("pTotalRecords")
        val debits = storedProcedure.getObject("pDDSummary", classOf[ResultSet])
        
        @tailrec
        def collectDebits(acc: Seq[DirectDebit] = Seq.empty): Seq[DirectDebit] =
          if (!debits.next()) {
            acc
          } else {
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

        UserDebits(debitTotal, collectDebits())
      }
    }

  def getEarliestPaymentDate(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] =
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("DD_PK.AddWorkingDays")

        storedProcedure.setDate("pInputDate", Date(baseDate.toEpochDay))
        storedProcedure.setInt("pNumberofWorkingDays", offsetWorkingDays)

        storedProcedure.registerOutParameter("pTotalRecords", Types.DATE)

        storedProcedure.execute()

        val date = storedProcedure.getDate("pOutputDate")

        EarliestPaymentDate(date.toLocalDate)
      }
    }
