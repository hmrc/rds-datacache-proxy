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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseOtherAssessments

import java.time.LocalDate

class OtherAssessmentsSpec extends AnyWordSpec with Matchers {

  "OtherAssessments JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(validResponseOtherAssessments)

      (json \ "periodStartDate").as[String] mustBe "2013-03-01"
      (json \ "periodEndDate").as[String] mustBe "2014-03-11"
      (json \ "total").as[Double] mustBe -24500.0
      (json \ "totalPeriodRecords").as[Int] mustBe 3

      (json \ "assessments").as[Seq[Assessments]].size mustBe 3

      val assessments1 = (json \ "assessments")(0)
      (assessments1 \ "dateRaised").as[String] mustBe "2014-01-01"
      (assessments1 \ "periodStartDate").as[String] mustBe "2014-04-01"
      (assessments1 \ "periodEndDate").as[String] mustBe "2014-06-30"
      (assessments1 \ "amount").as[Double] mustBe -9500.0

      val assessments2 = (json \ "assessments")(1)
      (assessments2 \ "dateRaised").as[String] mustBe "2014-01-02"
      (assessments2 \ "periodStartDate").as[String] mustBe "2014-01-01"
      (assessments2 \ "periodEndDate").as[String] mustBe "2014-03-31"
      (assessments2 \ "amount").as[Double] mustBe -8000.0

      val assessments3 = (json \ "assessments")(2)
      (assessments3 \ "dateRaised").as[String] mustBe "2014-01-03"
      (assessments3 \ "periodStartDate").as[String] mustBe "2013-10-01"
      (assessments3 \ "periodEndDate").as[String] mustBe "2013-12-31"
      (assessments3 \ "amount").as[Double] mustBe -7000.0
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 3, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 3, 11)}",
           "total":-24500.0,
           "totalPeriodRecords":3,
           "assessments":[
             {
                "dateRaised":"${LocalDate.of(2014, 1, 1)}",
                "periodStartDate": "${LocalDate.of(2014, 4, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 6, 30)}",
                "amount":-9500.0
              },
              {
                "dateRaised":"${LocalDate.of(2014, 1, 2)}",
                "periodStartDate": "${LocalDate.of(2014, 1, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 3, 31)}",
                "amount":-8000.0
              },
              {
                "dateRaised":"${LocalDate.of(2014, 1, 3)}",
                "periodStartDate": "${LocalDate.of(2013, 10, 1)}",
                "periodEndDate":"${LocalDate.of(2013, 12, 31)}",
                "amount":-7000.0
              }
           ]
           }""".stripMargin
      )

      val result: JsResult[OtherAssessments] = json.validate[OtherAssessments]

      result mustBe JsSuccess(validResponseOtherAssessments)
    }

    "round-trip (write then read) should return same object" in {
      val original = validResponseOtherAssessments

      val json = Json.toJson(original)
      val parsed = json.as[OtherAssessments]

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

      val result = json.validate[OtherAssessments]

      result.isError mustBe true
    }

    "fail to deserialize when types are incorrect" in {
      val json = Json.parse(
        s"""{
           "periodStartDate":"${LocalDate.of(2013, 3, 1)}",
           "periodEndDate":"${LocalDate.of(2014, 3, 11)}",
           "total":-24500.0,
           "totalPeriodRecords":"3",
           "assessments":[
             {
                "dateRaised":"2014-01-01",
                "periodStartDate": "${LocalDate.of(2014, 4, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 6, 30)}",
                "amount":-9500.0
              },
              {
                "dateRaised":"2014-01-01",
                "periodStartDate": "${LocalDate.of(2014, 1, 1)}",
                "periodEndDate":"${LocalDate.of(2014, 3, 31)}",
                "amount":-8000.0
              },
              {
                "dateRaised":"2014-01-01",
                "periodStartDate": "${LocalDate.of(2013, 10, 1)}",
                "periodEndDate":"${LocalDate.of(2013, 12, 31)}",
                "amount":-7000.0
              }
           ]
           }""".stripMargin
      )

      val result = json.validate[OtherAssessments]

      result.isError mustBe true
    }
  }
}
