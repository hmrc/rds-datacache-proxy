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

class SubcontractorPrepopSpec extends AnyWordSpec with Matchers {

  "SubcontractorPrepopRecord JSON format" should {

    "serialise and deserialise correctly" in {
      val model = SubcontractorPrepopRecord(
        subcontractorType  = "I",
        subcontractorUtr   = "1123456789",
        verificationNumber = "12345678901",
        verificationSuffix = Some("AB"),
        title              = Some("Mr"),
        firstName          = Some("Bob"),
        secondName         = None,
        surname            = Some("Builder"),
        tradingName        = Some("Bob Builder Ltd")
      )

      val json = Json.toJson(model)

      json.as[SubcontractorPrepopRecord] mustBe model
    }
  }

  "PrePopSubcontractor JSON format" should {

    "serialise and deserialise correctly" in {
      val model = PrePopSubcontractor(
        subcontractorType  = "I",
        utr                = "1123456789",
        verificationNumber = "12345678901",
        verificationSuffix = "AB",
        title              = "Mr",
        firstName          = "Bob",
        secondName         = "Middle",
        surname            = "Builder"
      )

      val json = Json.toJson(model)

      json.as[PrePopSubcontractor] mustBe model
    }
  }

  "PrePopSubcontractorsBody JSON format" should {

    "serialise and deserialise correctly" in {
      val sub1 = PrePopSubcontractor(
        subcontractorType  = "I",
        utr                = "1123456789",
        verificationNumber = "12345678901",
        verificationSuffix = "AB",
        title              = "Mr",
        firstName          = "Bob",
        secondName         = "",
        surname            = "Builder"
      )

      val sub2 = PrePopSubcontractor(
        subcontractorType  = "O",
        utr                = "2234567890",
        verificationNumber = "22345678901",
        verificationSuffix = "",
        title              = "Ms",
        firstName          = "Alice",
        secondName         = "Marie",
        surname            = "Smith"
      )

      val model = PrePopSubcontractorsBody(
        response       = 0,
        subcontractors = Seq(sub1, sub2)
      )

      val json = Json.toJson(model)

      json.as[PrePopSubcontractorsBody] mustBe model
    }
  }

  "PrePopSubcontractorsResponse JSON format" should {

    "serialise and deserialise correctly" in {
      val known = PrepopKnownFacts(
        taxOfficeNumber    = "123",
        taxOfficeReference = "AB456",
        agentOwnReference  = "123PA12345678"
      )

      val sub = PrePopSubcontractor(
        subcontractorType  = "I",
        utr                = "1123456789",
        verificationNumber = "12345678901",
        verificationSuffix = "AB",
        title              = "Mr",
        firstName          = "Bob",
        secondName         = "",
        surname            = "Builder"
      )

      val body = PrePopSubcontractorsBody(
        response       = 0,
        subcontractors = Seq(sub)
      )

      val model = PrePopSubcontractorsResponse(
        knownfacts           = known,
        prePopSubcontractors = body
      )

      val json = Json.toJson(model)

      json.as[PrePopSubcontractorsResponse] mustBe model
    }
  }
}
