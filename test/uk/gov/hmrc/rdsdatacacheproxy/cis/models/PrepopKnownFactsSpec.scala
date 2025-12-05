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

package uk.gov.hmrc.rdsdatacacheproxy.cis.models

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*

class PrepopKnownFactsSpec extends AnyWordSpec with Matchers {

  "PrepopKnownFactsRequest JSON format" should {

    "serialise and deserialise correctly" in {
      val model = PrepopKnownFacts(
        taxOfficeNumber    = "123",
        taxOfficeReference = "AB456",
        agentOwnReference  = "123PA12345678"
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "taxOfficeNumber"    -> "123",
        "taxOfficeReference" -> "AB456",
        "agentOwnReference"  -> "123PA12345678"
      )

      json.as[PrepopKnownFacts] mustBe model
    }
  }
}
