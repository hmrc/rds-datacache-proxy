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


import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import scala.concurrent.{ExecutionContext, Future}
import java.sql.ResultSet
import oracle.jdbc.OracleTypes
import scala.annotation.tailrec
import uk.gov.hmrc.rdsdatacacheproxy.models.{MonthlyReturn, UserMonthlyReturns} 

trait CisMonthlyReturnSource {
  def findInstanceId(taxOfficeNumber: String, taxOfficeReference: String): Future[Option[String]]
  def getMonthlyReturns(instanceId: String): Future[UserMonthlyReturns]
}

@Singleton
class CisDatacacheRepository @Inject()(
                                        @NamedDatabase("cis") db: Database        // <- binds to db.cis (not db.default)
                                      )(implicit ec: ExecutionContext)
  extends CisMonthlyReturnSource with Logging {
  
  override def findInstanceId(taxOfficeNumber: String, taxOfficeReference: String): Future[Option[String]] = {
    logger.info(s"[CIS] findInstanceId(TaxOfficeNumber=$taxOfficeNumber, TaxOfficeReference=$taxOfficeReference)")
    
    Future {
      db.withConnection { conn =>
        val cs = conn.prepareCall("{ call ECISR_SEARCH_PK.getCISTaxpayerByTaxReference(?, ?, ?) }")
        try {
          cs.setString(1, taxOfficeNumber)
          cs.setString(2, taxOfficeReference)
          cs.registerOutParameter(3, OracleTypes.CURSOR)
          cs.execute()

          val rs = cs.getObject(3, classOf[ResultSet]) // c_TxpByTaxRef
          try {
            // take the first row, if any
            if (rs.next()) {
              val idOpt = Option(rs.getString("UNIQUE_ID")).map(_.trim).filter(_.nonEmpty)

              // if more than one row, log and still return first
              if (rs.next()) {
                logger.warn(s"[CIS] findInstanceId: multiple rows for TON=$taxOfficeNumber, TOR=$taxOfficeReference; using first UNIQUE_ID")
              }

              idOpt
            } else {
              None
            }
          } finally if (rs != null) rs.close()
        } finally cs.close()
      }
    }
  }
  
  
  override def getMonthlyReturns(instanceId: String): Future[UserMonthlyReturns] = {
    logger.info(s"[CIS] getMonthlyReturns(instanceId=$instanceId)")
    Future {
      db.withConnection { conn =>
        val cs = conn.prepareCall("{ call MONTHLY_RETURN_PROCS_2016.Get_All_Monthly_Returns(?, ?, ?) }")
        cs.setString(1, instanceId)
        cs.registerOutParameter(2, OracleTypes.CURSOR) // p_schemes
        cs.registerOutParameter(3, OracleTypes.CURSOR) // p_monthly_returns
        cs.execute()

        val rsScheme = cs.getObject(2, classOf[ResultSet])
        try () finally if (rsScheme != null) rsScheme.close()

        val rsMonthly = cs.getObject(3, classOf[ResultSet])
        val returns =
          try collectMonthlyReturns(rsMonthly)
          finally if (rsMonthly != null) rsMonthly.close()

        UserMonthlyReturns(returns)
      }
    }
  }

  @tailrec
  private def collectMonthlyReturns(rs: ResultSet, acc: Seq[MonthlyReturn] = Nil): Seq[MonthlyReturn] =
    if (!rs.next()) acc
    else {
      val mr = MonthlyReturn(
        monthlyReturnId = rs.getLong("monthly_return_id"),
        taxYear         = rs.getInt("tax_year"),
        taxMonth        = rs.getInt("tax_month"),
        nilReturnIndicator     = Option(rs.getString("nil_return_indicator")),
        decEmpStatusConsidered = Option(rs.getString("dec_emp_status_considered")),
        decAllSubsVerified     = Option(rs.getString("dec_all_subs_verified")),
        decInformationCorrect  = Option(rs.getString("dec_information_correct")),
        decNoMoreSubPayments   = Option(rs.getString("dec_no_more_sub_payments")),
        decNilReturnNoPayments = Option(rs.getString("dec_nil_return_no_payments")),
        status                 = Option(rs.getString("status")),
        lastUpdate             = Option(rs.getTimestamp("last_update")).map(_.toLocalDateTime),
        amendment              = Option(rs.getString("amendment")),
        supersededBy           = { val v = rs.getLong("superseded_by"); if (rs.wasNull()) None else Some(v) }
      )
      collectMonthlyReturns(rs, acc :+ mr)
    }
}