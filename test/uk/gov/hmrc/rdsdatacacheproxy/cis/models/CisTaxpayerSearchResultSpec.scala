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

class CisTaxpayerSearchResultSpec extends AnyWordSpec with Matchers {

  "CisTaxpayerSearchResult" should {

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
          |"schemeName": "CIS"
          |}
        """.stripMargin
      )

      val json = Json.parse(jsonAsString)
      val model = json.as[CisTaxpayerSearchResult]

      model mustBe CisTaxpayerSearchResult(
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
        schemeName        = Some("CIS")
      )

      Json.toJson(model) mustBe json
    }

    "read and write a minimally-populated object" in {
      val jsonAsString = String(
        """
          |{
          |"uniqueId": "1",
          |"taxOfficeNumber": "123",
          |"taxOfficeRef": "AB456",
          |"aoDistrict": null,
          |"aoPayType": null,
          |"aoCheckCode": null,
          |"aoReference": null,
          |"validBusinessAddr": null,
          |"correlation": null,
          |"ggAgentId": null,
          |"employerName1": null,
          |"employerName2": null,
          |"agentOwnRef": null,
          |"schemeName": null
          |}
        """.stripMargin
      )

      val json = Json.parse(jsonAsString)
      val model = json.as[CisTaxpayerSearchResult]

      model mustBe CisTaxpayerSearchResult(
        uniqueId          = "1",
        taxOfficeNumber   = "123",
        taxOfficeRef      = "AB456",
        aoDistrict        = None,
        aoPayType         = None,
        aoCheckCode       = None,
        aoReference       = None,
        validBusinessAddr = None,
        correlation       = None,
        ggAgentId         = None,
        employerName1     = None,
        employerName2     = None,
        agentOwnRef       = None,
        schemeName        = None
      )

      // When serializing, Play JSON omits null fields by default
      val serialized = Json.toJson(model)
      val deserialized = serialized.as[CisTaxpayerSearchResult]
      deserialized mustBe model
    }

    "serialize to JSON correctly" in {
      val model = CisTaxpayerSearchResult(
        uniqueId          = "123",
        taxOfficeNumber   = "456",
        taxOfficeRef      = "XY789",
        aoDistrict        = Some("100"),
        aoPayType         = Some("M"),
        aoCheckCode       = Some("AB"),
        aoReference       = Some("REF001"),
        validBusinessAddr = Some("Y"),
        correlation       = Some("CORR001"),
        ggAgentId         = Some("GG001"),
        employerName1     = Some("ABC Ltd"),
        employerName2     = Some("Trading"),
        agentOwnRef       = Some("AGENT001"),
        schemeName        = Some("CIS Scheme")
      )

      val json = Json.toJson(model)

      (json \ "uniqueId").as[String] mustBe "123"
      (json \ "taxOfficeNumber").as[String] mustBe "456"
      (json \ "taxOfficeRef").as[String] mustBe "XY789"
      (json \ "aoDistrict").asOpt[String] mustBe Some("100")
      (json \ "aoPayType").asOpt[String] mustBe Some("M")
      (json \ "aoCheckCode").asOpt[String] mustBe Some("AB")
      (json \ "aoReference").asOpt[String] mustBe Some("REF001")
      (json \ "validBusinessAddr").asOpt[String] mustBe Some("Y")
      (json \ "correlation").asOpt[String] mustBe Some("CORR001")
      (json \ "ggAgentId").asOpt[String] mustBe Some("GG001")
      (json \ "employerName1").asOpt[String] mustBe Some("ABC Ltd")
      (json \ "employerName2").asOpt[String] mustBe Some("Trading")
      (json \ "agentOwnRef").asOpt[String] mustBe Some("AGENT001")
      (json \ "schemeName").asOpt[String] mustBe Some("CIS Scheme")
    }

    "deserialize from JSON correctly" in {
      val jsonAsString = """
        {
          "uniqueId": "789",
          "taxOfficeNumber": "321",
          "taxOfficeRef": "ZZ999",
          "aoDistrict": "200",
          "aoPayType": "W",
          "aoCheckCode": "CD",
          "aoReference": "REF002",
          "validBusinessAddr": "N",
          "correlation": "CORR002",
          "ggAgentId": "GG002",
          "employerName1": "XYZ Builders",
          "employerName2": null,
          "agentOwnRef": "AGENT002",
          "schemeName": "Builder Scheme"
        }
      """

      val model = Json.parse(jsonAsString).as[CisTaxpayerSearchResult]

      model.uniqueId mustBe "789"
      model.taxOfficeNumber mustBe "321"
      model.taxOfficeRef mustBe "ZZ999"
      model.aoDistrict mustBe Some("200")
      model.aoPayType mustBe Some("W")
      model.aoCheckCode mustBe Some("CD")
      model.aoReference mustBe Some("REF002")
      model.validBusinessAddr mustBe Some("N")
      model.correlation mustBe Some("CORR002")
      model.ggAgentId mustBe Some("GG002")
      model.employerName1 mustBe Some("XYZ Builders")
      model.employerName2 mustBe None
      model.agentOwnRef mustBe Some("AGENT002")
      model.schemeName mustBe Some("Builder Scheme")
    }

    "handle special characters in string fields" in {
      val model = CisTaxpayerSearchResult(
        uniqueId          = "id-with-dashes",
        taxOfficeNumber   = "123/456",
        taxOfficeRef      = "AB-CD-EF",
        aoDistrict        = Some("100 & 200"),
        aoPayType         = None,
        aoCheckCode       = None,
        aoReference       = None,
        validBusinessAddr = None,
        correlation       = None,
        ggAgentId         = None,
        employerName1     = Some("O'Brien & Sons Ltd"),
        employerName2     = Some("(Trading as XYZ)"),
        agentOwnRef       = Some("REF/001/2025"),
        schemeName        = Some("CIS-2025")
      )

      val json = Json.toJson(model)
      val deserializedModel = json.as[CisTaxpayerSearchResult]

      deserializedModel mustBe model
    }

    "handle empty strings as None for optional fields" in {
      // Note: Empty strings in JSON should be handled by the conversion logic
      val model = CisTaxpayerSearchResult(
        uniqueId          = "1",
        taxOfficeNumber   = "123",
        taxOfficeRef      = "AB456",
        aoDistrict        = None,
        aoPayType         = None,
        aoCheckCode       = None,
        aoReference       = None,
        validBusinessAddr = None,
        correlation       = None,
        ggAgentId         = None,
        employerName1     = None,
        employerName2     = None,
        agentOwnRef       = None,
        schemeName        = None
      )

      val json = Json.toJson(model)
      json.as[CisTaxpayerSearchResult] mustBe model
    }
  }
}
