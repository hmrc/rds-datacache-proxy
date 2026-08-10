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

import play.api.Logging

import java.sql.{CallableStatement, ResultSet}
import java.time.LocalDate

trait RepositoryDataSupport extends Logging {

  def optDate(i: Int, cs: CallableStatement): Option[LocalDate] = Option(cs.getDate(i)).map(_.toLocalDate)

  def optBigDecimal(i: Int, cs: CallableStatement): Option[BigDecimal] = Option(cs.getBigDecimal(i))

  def processResultSetList[T](cs: CallableStatement, position: Int, processor: ResultSet => T, context: String): List[T] = {
    val rs = cs.getObject(position, classOf[ResultSet])
    try
      if (rs != null) {
        val buffer = scala.collection.mutable.ListBuffer[T]()
        while (rs.next())
          buffer += processor(rs)
        buffer.toList
      } else {
        logger.info(s"$context, No result set found at position: $position null cursor")
        logger.info(s"$context, Returning empty list ")
        List.empty
      }
    finally
      if (rs != null) rs.close()
  }

}
