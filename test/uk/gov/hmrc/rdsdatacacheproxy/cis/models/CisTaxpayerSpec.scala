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

class CisTaxpayerSpec extends AnyWordSpec with Matchers {

  "CISTaxpayer" should {

    "read and write a full-populated object" in {
      val jsonAsString = String(
        """
          |{
          |"uniqueId": "1",
          |"taxOfficeNumber": "123",
          |"taxOfficeRef": "AB456",
          |"aoDistrict": "999",
          |"aoPayType": "CIS",
          |"aoCheckCode": "999",
          |"aoReference": "test",
          |"validBusinessAddr": "Y",
          |"correlation": "corr-123",
          |"ggAgentId": "agent-123",
          |"employerName1": "TEST LTD",
          |"employerName2": "GROUP",
          |"agentOwnRef": "ref-123",
          |"schemeName": "CIS",
          |"utr": "1234567890",
          |"enrolledSig": "test-sig"
          |}
        """.stripMargin
      )

      val json = Json.parse(jsonAsString)
      val model = json.as[CisTaxpayer]

      model mustBe CisTaxpayer(
        uniqueId          = "1",
        taxOfficeNumber   = "123",
        taxOfficeRef      = "AB456",
        aoDistrict        = Some("999"),
        aoPayType         = Some("CIS"),
        aoCheckCode       = Some("999"),
        aoReference       = Some("test"),
        validBusinessAddr = Some("Y"),
        correlation       = Some("corr-123"),
        ggAgentId         = Some("agent-123"),
        employerName1     = Some("TEST LTD"),
        employerName2     = Some("GROUP"),
        agentOwnRef       = Some("ref-123"),
        schemeName        = Some("CIS"),
        utr               = Some("1234567890"),
        enrolledSig       = Some("test-sig")
      )

      Json.toJson(model) mustBe json
    }
  }
}
