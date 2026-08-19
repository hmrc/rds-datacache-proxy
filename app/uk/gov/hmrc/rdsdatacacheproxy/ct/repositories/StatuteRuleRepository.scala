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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import com.google.inject.ImplementedBy
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.StatuteRuleItem

import java.sql.Date
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[StatuteRuleRepositoryImpl])
trait StatuteRuleRepository {
  def getStatuteRule(ruleRateKey: String, startDate: LocalDate, endDate: LocalDate): Future[Option[StatuteRuleItem]]
}

class StatuteRuleRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends StatuteRuleRepository
    with Logging {

  def getStatuteRule(ruleRateKey: String, startDate: LocalDate, endDate: LocalDate): Future[Option[StatuteRuleItem]] = {
    logger.info(s"Input request: getStatuteRule $ruleRateKey - $startDate - $endDate")
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("{call CT_LNP_PK.getStatuteRule(?, ?, ?, ?, ?, ?, ?, ?)}")
        try {

          cs.setString(1, ruleRateKey) // p_Rule_Rate_Key

          cs.setDate(2, Date.valueOf(startDate)) // p_Start_Date
          cs.setDate(3, Date.valueOf(endDate)) // p_End_Date

          cs.registerOutParameter(4, java.sql.Types.DATE) // p_Statute_Rule_Start_Date
          cs.registerOutParameter(5, java.sql.Types.DATE) // p_Statute_Rule_End_Date
          cs.registerOutParameter(6, java.sql.Types.NUMERIC) // p_No_Of_Days
          cs.registerOutParameter(7, java.sql.Types.DECIMAL) // p_Statute_Rule_Amount
          cs.registerOutParameter(8, java.sql.Types.DECIMAL) // p_Statute_Rule_Rate

          cs.execute()

          val record = StatuteRuleItem(
            ruleStartDate = Option(cs.getDate(4)).map(_.toLocalDate),
            ruleEndDate   = Option(cs.getDate(5)).map(_.toLocalDate),
            numberOfDays  = Option(cs.getInt(6)),
            ruleAmount    = Option(cs.getBigDecimal(7)),
            ruleRate      = Option(cs.getBigDecimal(8))
          )
          Some(record)

        } catch {
          case sqlException: java.sql.SQLException if sqlException.getMessage.contains("no data found") =>
            logger.info(s"[StatuteRuleRepositoryImpl][getStatuteRule] no data found")
            None // no other exceptions to be caught
        } finally {
          cs.close()
        }

      }
    }
  }

}
