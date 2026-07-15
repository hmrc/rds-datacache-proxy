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

import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.TaxTransactionsItem

import java.sql.ResultSet
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait TaxTransactionsDataSource {
  def getTaxTransactions(taxRef: Long, accPeriod: Long): Future[List[TaxTransactionsItem]]
}

@Singleton
class TaxTransactionsDataCacheRepository @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends TaxTransactionsDataSource
    with Logging {

  override def getTaxTransactions(taxRef: Long, accPeriod: Long): Future[List[TaxTransactionsItem]] = {
    logger.info(s"getTaxTransactions input, taxRef: $taxRef, accPeriod: $accPeriod")
    Future {
      db.withConnection { connection =>

        val storedProcedure = connection.prepareCall("{call CT_DC_PK.getTaxTransactionList(?, ?, ?)}")
        try {
          storedProcedure.setLong(1, taxRef) // IN  P_TAXPAYER_REFERENCE
          storedProcedure.setLong(2, accPeriod) // IN  P_ACCOUNTING_PERIOD
          storedProcedure.registerOutParameter(3, OracleTypes.CURSOR) // OUT P_LIST
          storedProcedure.execute()

          val rs = storedProcedure.getObject(3, classOf[ResultSet])
          val taxTransactions =
            if (rs == null) Nil
            else {
              try collectTaxTransactions(rs)
              finally rs.close()
            }
          taxTransactions
        } finally storedProcedure.close()
      }
    }
  }
}

private def collectTaxTransactions(rs: ResultSet): List[TaxTransactionsItem] = {
  Iterator
    .continually(rs.next())
    .takeWhile(identity)
    .map(_ =>
      TaxTransactionsItem(
        currentAmount         = rs.getBigDecimal("current_amount"),
        assessmentType        = rs.getString("assessment_type"),
        taxDate               = rs.getDate("tax_date").toLocalDate,
        correctionClaimSignal = Option(rs.getString("correction_claim_signal"))
      )
    )
    .toList
}
