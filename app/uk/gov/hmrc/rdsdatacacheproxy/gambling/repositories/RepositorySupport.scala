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

import java.sql.{CallableStatement, ResultSet}
import java.time.LocalDate

trait RepositorySupport {

  protected def optDate(i: Int, cs: CallableStatement): Option[LocalDate] =
    Option(cs.getDate(i)).map(_.toLocalDate)

  protected def optInt(i: Int, cs: CallableStatement): Option[Int] =
    Option(cs.getObject(i)).map {
      case bd: java.math.BigDecimal => bd.intValue()
      case n: java.lang.Number      => n.intValue()
      case other                    => other.toString.toInt
    }

  protected def optDecimalFromIndex(i: Int, cs: CallableStatement): Option[BigDecimal] =
    Option(cs.getBigDecimal(i))
      .map(BigDecimal(_))
      .orElse {
        Option(cs.getObject(i)).map(obj => BigDecimal(obj.toString))
      }

  protected def optDecimalFromLabel(s: String, rs: ResultSet): Option[BigDecimal] =
    Option(rs.getBigDecimal(s))
      .map(BigDecimal(_))
      .orElse {
        Option(rs.getObject(s)).map(obj => BigDecimal(obj.toString))
      }

  protected def closeQuietly(c: AutoCloseable): Unit =
    if (c != null)
      try c.close()
      catch {
        case _: Throwable => ()
      }
}
