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

package uk.gov.hmrc.rdsdatacacheproxy.pas.repositories

import java.sql.{CallableStatement, ResultSet}

trait RepositoryHelper {

  // Extracts a ResultSet cursor from a specific CallableStatement output position, transforms each row into a typed object, and safely closes the database cursor.
  def processResultSetSeq[T](cs: CallableStatement, position: Int, processor: ResultSet => T): Seq[T] = {
    val rs = cs.getObject(position, classOf[ResultSet])
    try
      if (rs != null) {
        val buffer = scala.collection.mutable.ListBuffer[T]()
        while (rs.next())
          buffer += processor(rs)
        buffer.toSeq
      } else Seq.empty
    finally
      if (rs != null) rs.close()
  }

}
