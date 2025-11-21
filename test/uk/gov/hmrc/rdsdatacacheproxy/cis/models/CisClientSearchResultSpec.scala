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

class CisClientSearchResultSpec extends AnyWordSpec with Matchers {

  "CisClientSearchResult" should {

    "read and write a full-populated object" in {
      val jsonAsString = String(
        """
          |{
          |  "clients": [
          |    {
          |      "uniqueId": "1",
          |      "taxOfficeNumber": "123",
          |      "taxOfficeRef": "AB001",
          |      "aoDistrict": "456",
          |      "aoPayType": "M",
          |      "aoCheckCode": "XY",
          |      "aoReference": "REF001",
          |      "validBusinessAddr": "Y",
          |      "correlation": "CORR001",
          |      "ggAgentId": "GG001",
          |      "employerName1": "ABC Construction Ltd",
          |      "employerName2": "Trading",
          |      "agentOwnRef": "AGENT001",
          |      "schemeName": "ABC Construction"
          |    }
          |  ],
          |  "totalCount": 100,
          |  "clientNameStartingCharacters": ["A", "B", "C"]
          |}
        """.stripMargin
      )

      val json = Json.parse(jsonAsString)
      val model = json.as[CisClientSearchResult]

      model.clients.length mustBe 1
      model.totalCount mustBe 100
      model.clientNameStartingCharacters mustBe List("A", "B", "C")

      model.clients.head.uniqueId mustBe "1"
      model.clients.head.taxOfficeNumber mustBe "123"
      model.clients.head.employerName1 mustBe Some("ABC Construction Ltd")

      Json.toJson(model) mustBe json
    }

    "read and write an empty result" in {
      val jsonAsString = String(
        """
          |{
          |  "clients": [],
          |  "totalCount": 0,
          |  "clientNameStartingCharacters": []
          |}
        """.stripMargin
      )

      val json = Json.parse(jsonAsString)
      val model = json.as[CisClientSearchResult]

      model mustBe CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      Json.toJson(model) mustBe json
    }

    "serialize to JSON correctly with multiple clients" in {
      val client1 = CisTaxpayerSearchResult(
        uniqueId          = "1",
        taxOfficeNumber   = "123",
        taxOfficeRef      = "AB001",
        aoDistrict        = Some("100"),
        aoPayType         = Some("M"),
        aoCheckCode       = Some("XY"),
        aoReference       = Some("REF001"),
        validBusinessAddr = Some("Y"),
        correlation       = Some("CORR001"),
        ggAgentId         = Some("GG001"),
        employerName1     = Some("ABC Ltd"),
        employerName2     = None,
        agentOwnRef       = Some("AGENT001"),
        schemeName        = Some("ABC")
      )

      val client2 = CisTaxpayerSearchResult(
        uniqueId          = "2",
        taxOfficeNumber   = "456",
        taxOfficeRef      = "CD002",
        aoDistrict        = Some("200"),
        aoPayType         = Some("W"),
        aoCheckCode       = Some("ZZ"),
        aoReference       = Some("REF002"),
        validBusinessAddr = Some("N"),
        correlation       = Some("CORR002"),
        ggAgentId         = Some("GG002"),
        employerName1     = Some("XYZ Builders"),
        employerName2     = Some("Trading"),
        agentOwnRef       = Some("AGENT002"),
        schemeName        = Some("XYZ")
      )

      val model = CisClientSearchResult(
        clients                      = List(client1, client2),
        totalCount                   = 2,
        clientNameStartingCharacters = List("A", "X")
      )

      val json = Json.toJson(model)

      (json \ "clients").as[List[CisTaxpayerSearchResult]].length mustBe 2
      (json \ "totalCount").as[Int] mustBe 2
      (json \ "clientNameStartingCharacters").as[List[String]] mustBe List("A", "X")
    }

    "deserialize from JSON correctly" in {
      val jsonAsString = """
        {
          "clients": [
            {
              "uniqueId": "10",
              "taxOfficeNumber": "999",
              "taxOfficeRef": "ZZ999",
              "schemeName": "Test Scheme"
            }
          ],
          "totalCount": 50,
          "clientNameStartingCharacters": ["T", "U", "V"]
        }
      """

      val model = Json.parse(jsonAsString).as[CisClientSearchResult]

      model.clients.length mustBe 1
      model.totalCount mustBe 50
      model.clientNameStartingCharacters mustBe List("T", "U", "V")

      model.clients.head.uniqueId mustBe "10"
      model.clients.head.taxOfficeNumber mustBe "999"
      model.clients.head.taxOfficeRef mustBe "ZZ999"
      model.clients.head.schemeName mustBe Some("Test Scheme")
    }

    "handle round-trip serialization and deserialization" in {
      val originalModel = CisClientSearchResult(
        clients = List(
          CisTaxpayerSearchResult(
            uniqueId          = "1",
            taxOfficeNumber   = "123",
            taxOfficeRef      = "AB001",
            aoDistrict        = None,
            aoPayType         = None,
            aoCheckCode       = None,
            aoReference       = None,
            validBusinessAddr = None,
            correlation       = None,
            ggAgentId         = None,
            employerName1     = Some("Test Ltd"),
            employerName2     = None,
            agentOwnRef       = None,
            schemeName        = None
          )
        ),
        totalCount                   = 1,
        clientNameStartingCharacters = List("T")
      )

      val json = Json.toJson(originalModel)
      val deserializedModel = json.as[CisClientSearchResult]

      deserializedModel.clients.length mustBe originalModel.clients.length
      deserializedModel.totalCount mustBe originalModel.totalCount
      deserializedModel.clientNameStartingCharacters mustBe originalModel.clientNameStartingCharacters
    }

    "handle large list of starting characters" in {
      val allLetters = ('A' to 'Z').map(_.toString).toList

      val model = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = allLetters
      )

      val json = Json.toJson(model)
      val deserializedModel = json.as[CisClientSearchResult]

      deserializedModel.clientNameStartingCharacters mustBe allLetters
    }

    "preserve order of clients and starting characters" in {
      val clients = (1 to 5).map { i =>
        CisTaxpayerSearchResult(
          uniqueId          = s"$i",
          taxOfficeNumber   = s"$i$i$i",
          taxOfficeRef      = s"REF$i",
          aoDistrict        = None,
          aoPayType         = None,
          aoCheckCode       = None,
          aoReference       = None,
          validBusinessAddr = None,
          correlation       = None,
          ggAgentId         = None,
          employerName1     = Some(s"Client $i"),
          employerName2     = None,
          agentOwnRef       = None,
          schemeName        = None
        )
      }.toList

      val chars = List("C", "B", "A", "E", "D")

      val model = CisClientSearchResult(
        clients                      = clients,
        totalCount                   = 5,
        clientNameStartingCharacters = chars
      )

      val json = Json.toJson(model)
      val deserializedModel = json.as[CisClientSearchResult]

      deserializedModel.clients.map(_.uniqueId) mustBe List("1", "2", "3", "4", "5")
      deserializedModel.clientNameStartingCharacters mustBe chars
    }
  }
}
