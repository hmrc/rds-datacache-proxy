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
}
