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

package uk.gov.hmrc.rdsdatacacheproxy.pas.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class PasCisModelsSpec extends AnyWordSpec with Matchers {

  "CreateCisAppealRequest" should {

    "serialize to JSON correctly" in {
      val request = CreateCisAppealRequest(
        penaltyId = 1L
      )

      val json = Json.toJson(request)

      (json \ "penaltyId").as[Long] mustBe 1L
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "penaltyId" -> 1L
      )

      val result = json.validate[CreateCisAppealRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.penaltyId mustBe 1L
    }

    "fail to deserialize when penaltyId is missing" in {
      val json = Json.obj(
      )

      val result = json.validate[CreateCisAppealRequest]

      result.isError mustBe true
    }
  }
  "CreateCisAppealResponse" should {

    "serialize to JSON correctly when created is true" in {
      val response = CreateCisAppealResponse(created = true)

      val json = Json.toJson(response)

      (json \ "created").as[Boolean] mustBe true
    }

    "serialize to JSON correctly when created is false" in {
      val response = CreateCisAppealResponse(created = false)

      val json = Json.toJson(response)

      (json \ "created").as[Boolean] mustBe false
    }

    "deserialize from JSON correctly when created is true" in {
      val json = Json.obj("created" -> true)

      val result = json.validate[CreateCisAppealResponse]

      result mustBe a[JsSuccess[_]]
      result.get.created mustBe true
    }

    "deserialize from JSON correctly when created is false" in {
      val json = Json.obj("created" -> false)

      val result = json.validate[CreateCisAppealResponse]

      result mustBe a[JsSuccess[_]]
      result.get.created mustBe false
    }

    "fail to deserialize when created field is missing" in {
      val json = Json.obj()

      val result = json.validate[CreateCisAppealResponse]

      result.isError mustBe true
    }
  }

  "GetCisAppealablePenaltyRequest" should {

    "serialize to JSON correctly" in {
      val request = GetCisAppealablePenaltyRequest(
        penaltyId             = 1L,
        hmrcOfficeNumber      = "840123",
        schemeReferenceNumber = "098345"
      )

      val json = Json.toJson(request)

      (json \ "penaltyId").as[Long] mustBe 1
      (json \ "hmrcOfficeNumber").as[String] mustBe "840123"
      (json \ "schemeReferenceNumber").as[String] mustBe "098345"
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "penaltyId"             -> 1,
        "hmrcOfficeNumber"      -> "840123",
        "schemeReferenceNumber" -> "098345"
      )

      val result = json.validate[GetCisAppealablePenaltyRequest]

      result mustBe a[JsSuccess[_]]
      val request = result.get

      request.penaltyId mustBe 1
      request.hmrcOfficeNumber mustBe "840123"
      request.schemeReferenceNumber mustBe "098345"
    }

    "fail to deserialize when data is missing" in {
      val json = Json.obj(
        "hmrcOfficeNumber"      -> "840123",
        "schemeReferenceNumber" -> "098345"
      )

      val result = json.validate[GetCisAppealablePenaltyRequest]

      result.isError mustBe true
    }
  }

  "GetCisAppealablePenaltyResponse" should {

    "serialize to JSON correctly" in {
      val response = GetCisAppealablePenaltyResponse(
        uniqueId         = Some(10001),
        issueDate        = Some(LocalDate.of(2023, 1, 1)),
        taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
        penaltyType      = Some("CIS003"),
        penaltyAmount    = Some(BigDecimal(999.23))
      )

      val json = Json.toJson(response)

      (json \ "uniqueId").as[Int] mustBe 10001
      (json \ "issueDate").as[String] mustBe "2023-01-01"
      (json \ "taxPeriodEndDate").as[String] mustBe "2023-04-01"
      (json \ "penaltyType").as[String] mustBe "CIS003"
      (json \ "penaltyAmount").as[Double] mustBe 999.23
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "uniqueId"         -> 10001,
        "issueDate"        -> "2023-01-01",
        "taxPeriodEndDate" -> "2023-04-01",
        "penaltyType"      -> "CIS003",
        "penaltyAmount"    -> 999.23
      )

      val result = json.validate[GetCisAppealablePenaltyResponse]

      result mustBe a[JsSuccess[_]]
      result.get.uniqueId mustBe Some(10001)
      result.get.issueDate mustBe Some(LocalDate.of(2023, 1, 1))
      result.get.taxPeriodEndDate mustBe Some(LocalDate.of(2023, 4, 1))
      result.get.penaltyType mustBe Some("CIS003")
      result.get.penaltyAmount mustBe Some(999.23)
    }

    "fail to deserialize when type is incorrect" in {
      val json = Json.obj(
        "uniqueId" -> "10001"
      )

      val result = json.validate[GetCisAppealablePenaltyResponse]

      result.isError mustBe true
    }
  }
}
