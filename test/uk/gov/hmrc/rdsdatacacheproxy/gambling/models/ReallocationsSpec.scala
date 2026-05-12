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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResult, JsSuccess, Json}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseReallocationsIn

import java.time.LocalDate

class ReallocationsSpec extends AnyWordSpec with Matchers {

  "Reallocations JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseReallocationsIn)

      (json \ "periodStartDate").as[String] mustBe "2013-03-01"
      (json \ "periodEndDate").as[String] mustBe "2014-03-11"
      (json \ "total").as[Double] mustBe 24500.0
      (json \ "totalRecords").as[Int] mustBe 3

      (json \ "items").as[Seq[ReallocationItem]].size mustBe 3

      val items1 = (json \ "items")(0)
      (items1 \ "dateProcessed").as[String] mustBe "2014-04-01"
      (items1 \ "amount").as[Double] mustBe 9500.0

      val items2 = (json \ "items")(1)
      (items2 \ "dateProcessed").as[String] mustBe "2014-01-01"
      (items2 \ "amount").as[Double] mustBe 8000.0

      val items3 = (json \ "items")(2)
      (items3 \ "dateProcessed").as[String] mustBe "2013-10-01"
      (items3 \ "amount").as[Double] mustBe 7000.0
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 3, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 3, 11)}",
           "total":24500.0,
           "totalRecords":3,
           "items":[
             {
                "dateProcessed": "${LocalDate.of(2014, 4, 1)}",
                "amount":9500.0
              },
              {
                "dateProcessed": "${LocalDate.of(2014, 1, 1)}",
                "amount":8000.0
              },
              {
                "dateProcessed": "${LocalDate.of(2013, 10, 1)}",
                "amount":7000.0
              }
           ]
           }""".stripMargin
      )

      val result: JsResult[Reallocations] = json.validate[Reallocations]

      result mustBe JsSuccess(validResponseReallocationsIn)
    }

    "round-trip (write then read) should return same object" in {
      val original = validResponseReallocationsIn

      val json = Json.toJson(original)
      val parsed = json.as[Reallocations]

      parsed mustBe original
    }

    "fail to deserialize when fields are missing" in {
      val json = Json.parse(
        """
          |{
          |  "periodStartDate":"${LocalDate.of(2013, 3, 1)}"
          |}
          |""".stripMargin
      )

      val result = json.validate[Reallocations]

      result.isError mustBe true
    }

    "fail to deserialize when types are incorrect" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 3, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 3, 11)}",
           "total":24500.0,
           "totalRecords":"3",
           "items":[
             {
                "dateProcessed": "${LocalDate.of(2014, 4, 1)}",
                "amount":9500.0
              },
              {
                "dateProcessed": "${LocalDate.of(2014, 1, 1)}",
                "amount":8000.0
              },
              {
                "dateProcessed": "${LocalDate.of(2013, 10, 1)}",
                "amount":7000.0
              }
           ]
           }""".stripMargin
      )

      val result = json.validate[Reallocations]

      result.isError mustBe true
    }
  }
}
