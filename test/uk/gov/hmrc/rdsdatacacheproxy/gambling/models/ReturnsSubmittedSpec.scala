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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseReturnsSubmitted

import java.time.LocalDate

class ReturnsSubmittedSpec extends AnyWordSpec with Matchers {

  "ReturnsSubmitted JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseReturnsSubmitted)

      (json \ "periodStartDate").as[String] mustBe "2013-03-01"
      (json \ "periodEndDate").as[String] mustBe "2014-03-11"
      (json \ "total").as[Double] mustBe -24500.0
      (json \ "totalPeriodRecords").as[Int] mustBe 3

      (json \ "amountDeclared").as[Seq[AmountDeclared]].size mustBe 3

      val amountDeclared1 = (json \ "amountDeclared")(0)
      (amountDeclared1 \ "descriptionCode").as[Int] mustBe 2650
      (amountDeclared1 \ "periodStartDate").as[String] mustBe "2014-04-01"
      (amountDeclared1 \ "periodEndDate").as[String] mustBe "2014-06-30"
      (amountDeclared1 \ "amount").as[Double] mustBe -9500.0

      val amountDeclared2 = (json \ "amountDeclared")(1)
      (amountDeclared2 \ "descriptionCode").as[Int] mustBe 2650
      (amountDeclared2 \ "periodStartDate").as[String] mustBe "2014-01-01"
      (amountDeclared2 \ "periodEndDate").as[String] mustBe "2014-03-31"
      (amountDeclared2 \ "amount").as[Double] mustBe -8000.0

      val amountDeclared3 = (json \ "amountDeclared")(2)
      (amountDeclared3 \ "descriptionCode").as[Int] mustBe 2650
      (amountDeclared3 \ "periodStartDate").as[String] mustBe "2013-10-01"
      (amountDeclared3 \ "periodEndDate").as[String] mustBe "2013-12-31"
      (amountDeclared3 \ "amount").as[Double] mustBe -7000.0
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 3, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 3, 11)}",
           "total":-24500.0,
           "totalPeriodRecords":3,
           "amountDeclared":[
             {
                "descriptionCode":2650,
                "periodStartDate": "${LocalDate.of(2014, 4, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 6, 30)}",
                "amount":-9500.0
              },
              {
                "descriptionCode":2650,
                "periodStartDate": "${LocalDate.of(2014, 1, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 3, 31)}",
                "amount":-8000.0
              },
              {
                "descriptionCode":2650,
                "periodStartDate": "${LocalDate.of(2013, 10, 1)}",
                "periodEndDate":"${LocalDate.of(2013, 12, 31)}",
                "amount":-7000.0
              }
           ]
           }""".stripMargin
      )

      val result: JsResult[ReturnsSubmitted] = json.validate[ReturnsSubmitted]

      result mustBe JsSuccess(validResponseReturnsSubmitted)
    }

    "round-trip (write then read) should return same object" in {
      val original = validResponseReturnsSubmitted

      val json = Json.toJson(original)
      val parsed = json.as[ReturnsSubmitted]

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

      val result = json.validate[ReturnsSubmitted]

      result.isError mustBe true
    }

    "fail to deserialize when types are incorrect" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 3, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 3, 11)}",
           "total":-24500.0,
           "totalPeriodRecords":"3",
           "amountDeclared":[
             {
                "descriptionCode":2650,
                "periodStartDate": "${LocalDate.of(2014, 4, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 6, 30)}",
                "amount":-9500.0
              },
              {
                "descriptionCode":2650,
                "periodStartDate": "${LocalDate.of(2014, 1, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 3, 31)}",
                "amount":-8000.0
              },
              {
                "descriptionCode":2650,
                "periodStartDate": "${LocalDate.of(2013, 10, 1)}",
                "periodEndDate":"${LocalDate.of(2013, 12, 31)}",
                "amount":-7000.0
              }
           ]
           }""".stripMargin
      )

      val result = json.validate[ReturnsSubmitted]

      result.isError mustBe true
    }
  }
}
