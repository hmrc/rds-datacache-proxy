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
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestAccural

import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[InterestAccuralListDatacacheRepositoryImpl])
trait InterestAccuralListDatacacheRepository {
  def getInterestAccuralList(taxRef: Long, accPeriod: Long, interestType: String): Future[List[InterestAccural]]
}

class InterestAccuralListDatacacheRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends InterestAccuralListDatacacheRepository
    with Logging {

  def getInterestAccuralList(taxRef: Long, accPeriod: Long, interestType: String): Future[List[InterestAccural]] = {
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call CT_DC_PK.getInterestAccrualList(?, ?, ?, ?)}")

        storedProcedure.setLong(1, taxRef)
        storedProcedure.setLong(2, accPeriod)
        storedProcedure.setString(3, interestType)
        storedProcedure.registerOutParameter(4, OracleTypes.CURSOR)

        storedProcedure.execute()

        val results = storedProcedure.getObject(3, classOf[ResultSet])

        try {
          val interestAccuralLists = Option(results).map(readInterestAccuralListTransaction).getOrElse(List.empty)
          interestAccuralLists
        } finally {
          storedProcedure.close()
        }
      }
    }
  }

  private def readInterestAccuralListTransaction(rs: ResultSet): List[InterestAccural] = {
    val buffer = ListBuffer[InterestAccural]()
    while (rs.next()) {
      buffer += InterestAccural(
        computationAmount       = Option(rs.getBigDecimal("computation_amount")).get,
        interestAccrualFromDate = Option(rs.getDate("interest_accrual_from_date")).map(_.toLocalDate).get,
        interestAccrualToDate   = Option(rs.getDate("interest_accrual_from_date")).map(_.toLocalDate).get,
        interestRate            = Option(rs.getBigDecimal("interest_rate")).get,
        interestAmount          = Option(rs.getBigDecimal("interest_amount")).get,
        apEndDate               = Option(rs.getDate("ap_end_date")).map(_.toLocalDate).get
      )
    }
    buffer.toList
  }

}
