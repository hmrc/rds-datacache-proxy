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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{AmountDeclared, ReturnsSubmitted}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait GamblingReturnsDataSource {
  def getReturnsSubmitted(regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReturnsSubmitted]
}

@Singleton
class GamblingReturnsDataCacheRepository @Inject() (@NamedDatabase("gambling") db: Database)(implicit ec: ExecutionContext)
    extends GamblingReturnsDataSource
    with Logging {

  override def getReturnsSubmitted(regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReturnsSubmitted] = {

    logger.info(
      s"[GamblingReturnsDataCacheRepository][getReturnsSubmitted] regNumber=$regNumber paginationStart=$paginationStart paginationMaxRows=$paginationMaxRows"
    )

    Future {
      db.withConnection { connection =>

        val cs = connection.prepareCall("{ call GTR_LNP_PK.getGTRReturnsSubmitted(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_REG_NUMBER
          cs.setInt(2, paginationStart) // IN  P_PAGINATION_START
          cs.setInt(3, paginationMaxRows) // IN  P_PAGINATION_MAX_ROWS
          cs.registerOutParameter(4, java.sql.Types.DATE) // OUT P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(5, java.sql.Types.DATE) // OUT P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_TOTAL (NUMBER)
          cs.registerOutParameter(7, java.sql.Types.NUMERIC) // OUT P_TOTAL_RECORDS (NUMBER)
          cs.registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR) // OUT C_AMOUNT_DECLARED (REF CURSOR)
          cs.execute()

          val amountDeclared: List[AmountDeclared] = {
            val rs = cs.getObject(8).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[AmountDeclared]
                while (rs.next()) {
                  b += AmountDeclared(
                    descriptionCode = Option(rs.getInt("p_desc_code")),
                    periodStartDate = Option(rs.getDate("p_period_start").toLocalDate),
                    periodEndDate   = Option(rs.getDate("p_period_end").toLocalDate),
                    amount          = optDecimalFromLabel("p_amount", rs)
                  )
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          ReturnsSubmitted(
            periodStartDate    = optDate(4, cs),
            periodEndDate      = optDate(5, cs),
            total              = optDecimalFromIndex(6, cs),
            totalPeriodRecords = optInt(7, cs),
            amountDeclared     = amountDeclared
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
