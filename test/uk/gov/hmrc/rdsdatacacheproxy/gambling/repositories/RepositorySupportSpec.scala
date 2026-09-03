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

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeParseException

class RepositorySupportSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val support = new RepositorySupport {}

  "optSystemDate" should {

    "return None when the column is null" in {
      val rs = mock[ResultSet]
      when(rs.getString("system_date")).thenReturn(null)

      support.optSystemDate("system_date", rs) shouldBe None
    }

    "parse a plain date with no time component" in {
      val rs = mock[ResultSet]
      when(rs.getString("system_date")).thenReturn("2026-05-31")

      support.optSystemDate("system_date", rs) shouldBe Some(LocalDate.of(2026, 5, 31))
    }

    "parse a date with a time component" in {
      val rs = mock[ResultSet]
      when(rs.getString("system_date")).thenReturn("2026-05-31 15:10:33")

      support.optSystemDate("system_date", rs) shouldBe Some(LocalDate.of(2026, 5, 31))
    }

    "read from the given column name" in {
      val rs = mock[ResultSet]
      when(rs.getString("some_other_date_column")).thenReturn("2026-01-15")

      support.optSystemDate("some_other_date_column", rs) shouldBe Some(LocalDate.of(2026, 1, 15))
    }

    "throw a DateTimeParseException carrying the offending text for unrecognised text" in {
      val rs = mock[ResultSet]
      when(rs.getString("system_date")).thenReturn("not-a-date")

      val ex = the[DateTimeParseException] thrownBy support.optSystemDate("system_date", rs)
      ex.getParsedString shouldBe "not-a-date"
      ex.getMessage should include("system_date")
    }

    "throw a DateTimeParseException for a value that matches the date shape but is not a valid calendar date" in {
      val rs = mock[ResultSet]
      when(rs.getString("system_date")).thenReturn("2026-13-40")

      a[DateTimeParseException] should be thrownBy support.optSystemDate("system_date", rs)
    }

    "ignore an out-of-range time portion since only the date is used" in {
      val rs = mock[ResultSet]
      when(rs.getString("system_date")).thenReturn("2026-05-31 99:99:99")

      support.optSystemDate("system_date", rs) shouldBe Some(LocalDate.of(2026, 5, 31))
    }
  }
}
