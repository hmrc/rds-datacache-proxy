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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{PayRepayReallocations, PayRepayReallocationsList}
import java.sql.*
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PayRepayReallocationRepositoryImpl])
trait PayRepayReallocationRepository {
  def getTotalAmounts(taxRef: Long, accPeriod: Long): Future[PayRepayReallocationsList]
}

class PayRepayReallocationRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends PayRepayReallocationRepository
    with Logging {

  def getTotalAmounts(taxRef: Long, accPeriod: Long): Future[Option[PayRepayReallocationsList]] = {
    Future {
      db.withConnection { connect =>
        val storedProcedure = connect.prepareCall("{call CT_DC_PK.getTotAmntsForPayRepayRealloc(?, ?, ?)}")

        storedProcedure.setLong(1, taxRef)
        storedProcedure.setLong(2, accPeriod)
        storedProcedure.registerOutParameter(3, OracleTypes.CURSOR)

        storedProcedure.execute()

        val payRepayReallocationsList = storedProcedure.getObject(3, classOf[ResultSet])

        try {
          val payRepayReallocations =
            Option(payRepayReallocationsList).map(readPayRepayReallocations).getOrElse(PayRepayReallocationsList(List.empty))
          payRepayReallocations
        } finally {
          storedProcedure.close()
        }
      }
    }
  }

  private def readPayRepayReallocations(rs: ResultSet): PayRepayReallocationsList = {
    val buffer = ListBuffer[PayRepayReallocations]()
    while (rs.next()) {
      buffer += PayRepayReallocations(
        totalAmountReoRfrRto = rs.getBigDecimal("totalAmountReoRfrRto"),
        totalAmountPayments  = rs.getBigDecimal("totalAmountPayments")
      )
    }
    PayRepayReallocationsList(buffer.toList)
  }
}
