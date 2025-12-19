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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class SchemePrepopSpec extends AnyWordSpec with Matchers {

  "SchemePrepop" should {

    "serialise and deserialise with utr present" in {
      val model = SchemePrepop(
        taxOfficeNumber        = "123",
        taxOfficeReference     = "AB123",
        accountOfficeReference = "AOREF1",
        utr                    = Some("1234567890"),
        schemeName             = "Test Scheme"
      )

      val json = Json.toJson(model)
      json.validate[SchemePrepop].asOpt mustBe Some(model)
    }

    "serialise and deserialise with utr absent" in {
      val model = SchemePrepop(
        taxOfficeNumber        = "123",
        taxOfficeReference     = "AB123",
        accountOfficeReference = "AOREF1",
        utr                    = None,
        schemeName             = "Test Scheme"
      )

      val json = Json.toJson(model)
      json.validate[SchemePrepop].asOpt mustBe Some(model)
    }
  }

  "PrePopContractorBody" should {

    "serialise and deserialise correctly" in {
      val model = PrePopContractorBody(
        schemeName = "Test Scheme",
        utr        = "1234567890",
        response   = 0
      )

      val json = Json.toJson(model)
      json.validate[PrePopContractorBody].asOpt mustBe Some(model)
    }
  }

  "PrePopContractorResponse" should {

    "serialise and deserialise correctly" in {
      val known = PrepopKnownFacts(
        taxOfficeNumber        = "123",
        taxOfficeReference     = "AB456",
        accountOfficeReference = "AOREF1"
      )

      val body = PrePopContractorBody(
        schemeName = "Test Scheme",
        utr        = "1234567890",
        response   = 0
      )

      val model = PrePopContractorResponse(
        knownfacts       = known,
        prePopContractor = body
      )

      val json = Json.toJson(model)
      json.validate[PrePopContractorResponse].asOpt mustBe Some(model)
    }
  }
}
