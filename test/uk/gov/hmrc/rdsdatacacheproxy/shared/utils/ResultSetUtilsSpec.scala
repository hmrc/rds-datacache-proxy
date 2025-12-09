/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.shared.utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import java.sql.ResultSet
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.ResultSetUtils.*

class ResultSetUtilsSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "ResultSetUtils" should {

    "trim non-null values in getTrimmedOpt" in {
      val rs = mock[ResultSet]
      when(rs.getString("COL")).thenReturn("  value  ")

      rs.getTrimmedOpt("COL") mustBe Some("value")
    }

    "return None from getTrimmedOpt when value is null" in {
      val rs = mock[ResultSet]
      when(rs.getString("COL")).thenReturn(null)

      rs.getTrimmedOpt("COL") mustBe None
    }

    "trim non-null values in getTrimmedOrNull" in {
      val rs = mock[ResultSet]
      when(rs.getString("COL")).thenReturn("  value  ")

      rs.getTrimmedOrNull("COL") mustBe "value"
    }

    "return null from getTrimmedOrNull when value is null" in {
      val rs = mock[ResultSet]
      when(rs.getString("COL")).thenReturn(null)

      rs.getTrimmedOrNull("COL") mustBe null
    }
  }
}
