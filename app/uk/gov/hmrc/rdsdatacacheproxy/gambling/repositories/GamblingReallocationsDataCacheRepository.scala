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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ReallocationItem, Reallocations, ReallocationsDetails, ReallocationsOut, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait GamblingReallocationsDataSource {
  def getReallocationsIn(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[Reallocations]
  def getReallocationsOut(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReallocationsOut]
  def getReallocationsDetails(regime: Regime, regNumber: String): Future[ReallocationsDetails]
}

@Singleton
class GamblingReallocationsDataCacheRepository @Inject() (
  @NamedDatabase("gambling") mgdDb: MGDDatabase,
  @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
)(implicit ec: ExecutionContext)
    extends GamblingReallocationsDataSource
    with RepositorySupport
    with Logging {

  override def getReallocationsIn(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[Reallocations] = {

    logger.info(
      s"[GamblingReallocationsDataCacheRepository][ReallocationsIn] regime=$regime, regNumber=$regNumber, paginationStart=$paginationStart, paginationMaxRows=$paginationMaxRows"
    )

    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDReallocationsInDetails(?, ?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsInDetails(?, ?, ?, ?, ?, ?, ?, ?) }")

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

          val reallocationItem: List[ReallocationItem] = {
            val rs = cs.getObject(8).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[ReallocationItem]
                while (rs.next()) {
                  b += ReallocationItem(
                    dateProcessed = Option(rs.getDate("p_date_processed")).map(_.toLocalDate),
                    amount        = optDecimalFromLabel("p_amount", rs)
                  )
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          Reallocations(
            periodStartDate = optDate(4, cs),
            periodEndDate   = optDate(5, cs),
            total           = optDecimalFromIndex(6, cs),
            totalRecords    = optInt(7, cs),
            items           = reallocationItem
          )

        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
  }

  override def getReallocationsOut(regime: Regime, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReallocationsOut] = {
    logger.info(
      s"[GamblingReallocationsDataCacheRepository][ReallocationsOut] regime= $regime, regNumber=$regNumber, paginationStart=$paginationStart, paginationMaxRows=$paginationMaxRows"
    )

    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDReallocationsOutDetails(?, ?, ?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsOutDetails(?, ?, ?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN  P_GTR_REGISTRATION_NUMBER
          cs.setInt(2, paginationStart) // IN  P_PAGINATION_START
          cs.setInt(3, paginationMaxRows) // IN  P_PAGINATION_MAX_ROWS
          cs.registerOutParameter(4, java.sql.Types.DATE) // OUT P_GTR_PERIOD_START_DATE
          cs.registerOutParameter(5, java.sql.Types.DATE) // OUT P_GTR_PERIOD_END_DATE
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_TOTAL (NUMBER)
          cs.registerOutParameter(7, java.sql.Types.NUMERIC) // OUT P_TOTAL_RECORDS (NUMBER)
          cs.registerOutParameter(8, oracle.jdbc.OracleTypes.CURSOR) // OUT C_REALLOCATIONS_OUT (REF CURSOR)
          cs.execute()

          val reallocations: List[ReallocationsOut.Reallocation] = {
            val rs = cs.getObject(8).asInstanceOf[java.sql.ResultSet]
            if (rs == null) Nil
            else {
              try {
                val b = List.newBuilder[ReallocationsOut.Reallocation]
                while (rs.next()) {
                  val maybeItem =
                    for
                      dateProcessed <- Option(rs.getDate("p_date_processed").toLocalDate)
                      amount        <- optDecimalFromLabel("p_amount", rs)
                    yield ReallocationsOut.Reallocation(dateProcessed, amount)
                  b.addAll(maybeItem.toList)
                }
                b.result()
              } finally closeQuietly(rs)
            }
          }

          ReallocationsOut(
            periodStartDate = optDate(4, cs),
            periodEndDate   = optDate(5, cs),
            total           = optDecimalFromIndex(6, cs).getOrElse(0),
            totalRecords    = optInt(7, cs).getOrElse(0),
            items           = reallocations
          )
        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
  }

  override def getReallocationsDetails(regime: Regime, regNumber: String): Future[ReallocationsDetails] = {
    logger.info(
      s"[GamblingReallocationsDataCacheRepository][ReallocationsDetails] regime= $regime, regNumber=$regNumber"
    )

    Future {
      getDb(regime, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs =
          regime match
            case Regime.MGD => connection.prepareCall("{ call MGD_LNP_PK.getMGDReallocationsDetails(?, ?, ?, ?, ?, ?) }")
            case _          => connection.prepareCall("{ call GTR_LNP_PK.getGTRReallocationsDetails(?, ?, ?, ?, ?, ?) }")

        try {
          cs.setString(1, regNumber) // IN P_REGISTRATION_NUMBER
          cs.registerOutParameter(2, java.sql.Types.DATE) // OUT P_PERIOD_START_DATE
          cs.registerOutParameter(3, java.sql.Types.DATE) // OUT P_PERIOD_END_DATE
          cs.registerOutParameter(4, java.sql.Types.DECIMAL) // OUT P_REALLOCATIONS_IN_AMOUNT (DECIMAL)
          cs.registerOutParameter(5, java.sql.Types.DECIMAL) // OUT P_REALLOCATIONS_OUT_AMOUNT (DECIMAL)
          cs.registerOutParameter(6, java.sql.Types.DECIMAL) // OUT P_TOTAL (DECIMAL)
          cs.execute()

          ReallocationsDetails(
            periodStartDate        = optDate(2, cs),
            periodEndDate          = optDate(3, cs),
            reallocationsInAmount  = optDecimalFromIndex(4, cs).getOrElse(0),
            reallocationsOutAmount = optDecimalFromIndex(5, cs).getOrElse(0),
            total                  = optDecimalFromIndex(6, cs).getOrElse(0)
          )
        } finally {
          closeQuietly(cs)
        }
      }
    }(ec)
  }
}
