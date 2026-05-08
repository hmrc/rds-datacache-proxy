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
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ReallocationsIn, ReallocationsInAmount}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait GamblingReallocationsDataSource {
  def getReallocationsIn(regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReallocationsIn]
}

@Singleton
class GamblingReallocationsDataCacheRepository @Inject() (@NamedDatabase("gambling") db: Database)(implicit ec: ExecutionContext)
    extends GamblingReallocationsDataSource
    with Logging {

  override def getReallocationsIn(regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReallocationsIn] = {

    logger.info(
      s"[GamblingReturnsDataCacheRepository][ReallocationsIn] regNumber=$regNumber paginationStart=$paginationStart paginationMaxRows=$paginationMaxRows"
    )

    Future {
      db.withConnection { connection =>

        val cs = connection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsInDetails(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REG_NUMBER
          cs.setInt(2, paginationStart) // IN  P_PAGINATION_START
          cs.setInt(3, paginationMaxRows) // IN  P_PAGINATION_MAX_ROWS
          cs.registerOutParameter(4, oracle.jdbc.OracleTypes.DATE) // P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(5, oracle.jdbc.OracleTypes.DATE) // P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(6, oracle.jdbc.OracleTypes.DECIMAL) // P_TOTAL (NUMBER)
          cs.registerOutParameter(7, oracle.jdbc.OracleTypes.NUMERIC) // P_TOTAL_RECORDS (NUMBER)
          cs.registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR) // C_REALLOCATIONS (REF CURSOR)
          cs.execute()

          val reallocationsInAmount: List[ReallocationsInAmount] = {
            val rs = cs.getObject(8).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[ReallocationsInAmount]
                while (rs.next()) {
                  b += ReallocationsInAmount(
                    dateProcessed = Option(rs.getDate("p_date_processed").toLocalDate),
                    amount        = optDecimalFromLabel("p_amount", rs)
                  )
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          ReallocationsIn(
            periodStartDate       = optDate(4, cs),
            periodEndDate         = optDate(5, cs),
            total                 = optDecimalFromIndex(6, cs),
            totalPeriodRecords    = optInt(7, cs),
            reallocationsInAmount = reallocationsInAmount
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
  }

  def optDate(i: Int, cs: java.sql.CallableStatement): Option[java.time.LocalDate] = Option(cs.getDate(i)).map(_.toLocalDate)

  def optInt(i: Int, cs: java.sql.CallableStatement): Option[Int] =
    Option(cs.getObject(i)).map {
      case bd: java.math.BigDecimal => bd.intValue()
      case n: java.lang.Number      => n.intValue()
      case other                    => other.toString.toInt
    }

  def optDecimalFromIndex(i: Int, cs: java.sql.CallableStatement): Option[BigDecimal] = {
    def alternativeMethodForMockito(idx: Int): Option[BigDecimal] = cs.getObject(idx) match {
      case o: AnyRef => Some(BigDecimal.decimal(o.toString.toDouble))
      case null      => None
    }

    Option(cs.getBigDecimal(i)) match {
      case Some(v1) => Option(v1)
      case _        => alternativeMethodForMockito(i)
    }
  }

  def optDecimalFromLabel(s: String, rs: java.sql.ResultSet): Option[BigDecimal] = {
    def alternativeMethodForMockito(idx: String): Option[BigDecimal] = rs.getObject(idx) match {
      case o: AnyRef => Some(BigDecimal.decimal(o.toString.toDouble))
      case null      => None
    }

    Option(rs.getBigDecimal(s)) match {
      case Some(v1) => Option(v1)
      case _        => alternativeMethodForMockito(s)
    }
  }

  def closeQuietly(c: AutoCloseable): Unit =
    if (c != null)
      try c.close()
      catch {
        case _: Throwable => ()
      }
}
