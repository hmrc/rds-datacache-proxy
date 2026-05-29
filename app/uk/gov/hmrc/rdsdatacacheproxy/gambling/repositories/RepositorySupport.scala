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

import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import java.sql.{CallableStatement, ResultSet}
import java.time.LocalDate

trait RepositorySupport {

  def optDate(i: Int, cs: CallableStatement): Option[LocalDate] = Option(cs.getDate(i)).map(_.toLocalDate)

  def optInt(i: Int, cs: CallableStatement): Option[Int] =
    Option(cs.getObject(i)).map {
      case bd: java.math.BigDecimal => bd.intValue()
      case n: java.lang.Number      => n.intValue()
      case other                    => other.toString.toInt
    }

  def optDecimalFromIndex(i: Int, cs: CallableStatement): Option[BigDecimal] = {
    def alternativeMethodForMockito(idx: Int): Option[BigDecimal] = cs.getObject(idx) match {
      case o: AnyRef => Some(BigDecimal.decimal(o.toString.toDouble))
      case null      => None
    }

    Option(cs.getBigDecimal(i)) match {
      case Some(v1) => Option(v1)
      case _        => alternativeMethodForMockito(i)
    }
  }

  def optDecimalFromLabel(s: String, rs: ResultSet): Option[BigDecimal] = {
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

  def getDb(regime: Regime, mgdDb: MGDDatabase, gtrDb: GTRDatabase): MGDDatabase | GTRDatabase =
    regime match
      case Regime.MGD => mgdDb
      case _          => gtrDb
}

object RepositorySupport:
  opaque type GTRDatabase = Database
  opaque type MGDDatabase = Database

  extension (db: MGDDatabase | GTRDatabase) def underlying: Database = db
