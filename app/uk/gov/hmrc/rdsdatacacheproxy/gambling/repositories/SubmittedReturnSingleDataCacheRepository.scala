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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import play.api.Logging
import play.api.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Regime, SubmittedReturnSingle}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SubmittedReturnSingleDataSource {
  def getSubmittedReturnSingle(regNumber: String, consecNo: Int): Future[Option[SubmittedReturnSingle]]
}

@Singleton
class SubmittedReturnSingleDataCacheRepository @Inject() (@NamedDatabase("gambling") mgdDb: MGDDatabase,
                                                          @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
                                                         )(implicit
  ec: ExecutionContext
) extends SubmittedReturnSingleDataSource
    with RepositorySupport
    with Logging {

  override def getSubmittedReturnSingle(regNumber: String, consecNo: Int): Future[Option[SubmittedReturnSingle]] =
    Future {
      getDb(Regime.MGD, mgdDb, gtrDb).underlying.withConnection { conn =>
        val cs = conn.prepareCall("{ call MGD_DC_RTN_PCK.GET_SINGLE_RETURN_V2(?, ?, ?) }")

        try {
          cs.setString(1, regNumber)
          cs.setInt(2, consecNo)
          cs.registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)
          cs.execute()

          val rs = cs.getObject(3).asInstanceOf[java.sql.ResultSet]

          try {
            if (rs.next()) {

              val result = for
                consecNo                     <- Option(rs.getInt("consec_no"))
                mgdPeriod                    <- Option(rs.getString("mgd_period"))
                submittedDate                <- Option(rs.getDate("submitted_date")).map(_.toLocalDate)
                ackRef                       <- Option(rs.getString("ack_ref"))
                noOfMachines                 <- Option(rs.getInt("no_of_machines_avail"))
                netTakingsHigherRate         <- optDecimalFromLabel("net_takings_higher_rate", rs)
                netTakingsStdRate            <- optDecimalFromLabel("net_takings_std_rate", rs)
                netTakingsLowerRate          <- optDecimalFromLabel("net_takings_lower_rate", rs)
                totalDueHigherRate           <- optDecimalFromLabel("total_due_higher_rate", rs)
                totalDueStdRate              <- optDecimalFromLabel("total_due_std_rate", rs)
                totalDueLowerRate            <- optDecimalFromLabel("total_due_lower_rate", rs)
                dutyPayable                  <- optDecimalFromLabel("duty_payable", rs)
                underDeclaredDuty            <- optDecimalFromLabel("under_declared_duty", rs)
                previousReturnAmount         <- optDecimalFromLabel("previous_return_amount", rs)
                negativeAmountCarriedForward <- optDecimalFromLabel("neg_amt_carry_forward", rs)
                totalNetDutyPayable          <- optDecimalFromLabel("total_net_duty_payable", rs)
              yield SubmittedReturnSingle(
                consecNo,
                mgdPeriod,
                submittedDate,
                ackRef,
                noOfMachines,
                netTakingsHigherRate,
                netTakingsStdRate,
                netTakingsLowerRate,
                totalDueHigherRate,
                totalDueStdRate,
                totalDueLowerRate,
                dutyPayable,
                underDeclaredDuty,
                previousReturnAmount,
                negativeAmountCarriedForward,
                totalNetDutyPayable
              )
              result
            } else {
              logger.info(s"[getSubmittedReturnSingle] Empty result set for regNumber=$regNumber consecNo=$consecNo")
              None
            }
          } finally {
            rs.close()
          }
        } finally {
          cs.close()
        }
      }
    }(ec)
}
