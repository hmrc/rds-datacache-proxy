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

package uk.gov.hmrc.rdsdatacacheproxy.gambling

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.ReturnsSubmitted
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingReturnsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GamblingReturnsControllerIntegrationSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class GamblingReturnsRdsStub extends GamblingReturnsDataSource {
    override def getReturnsSubmitted(regNumber: String, pageNo: Int, pageSize: Int) =
      Future {
        GamblingReturnsStubData.getReturnsSubmitted(regNumber, pageNo, pageSize)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GamblingReturnsDataSource].toInstance(new GamblingReturnsRdsStub)
      )
      .build()

  private final val endpoint = "/gambling/returns-submitted"
  private final val GBD = "gbd"

  "GET /gambling/returns-submitted (stubbed repo, no DB)" should {

    "return 200 with correct summary" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/XYZ00000000000?pageNo=1&pageSize=10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ReturnsSubmitted] mustBe ReturnsSubmitted(
        periodStartDate = Some(LocalDate.of(2013, 3, 1)),
        periodEndDate = Some(LocalDate.of(2014, 3, 11)),
        total = Some(0.00),
        totalPeriodRecords = Some(0),
        amountDeclared = Seq()
      )
    }

    "return 200 with correct summary when pageNo & pageSize NOT provided" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/$GBD/XYZ99999999999").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[ReturnsSubmitted] mustBe ReturnsSubmitted(
        periodStartDate = Some(LocalDate.of(2013, 3, 1)),
        periodEndDate = Some(LocalDate.of(2014, 3, 11)),
        total = Some(999.00),
        totalPeriodRecords = Some(99),
        amountDeclared = Seq()
      )
    }

//    "normalise lowercase input" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/xyz00000000012 ").futureValue
//      response.status mustBe OK
//      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
//    }
//
//    "return default values for unknown mgdRegNumber" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/XYZ99999999999").futureValue
//      response.status mustBe OK
//      (response.json \ "returnsDue").as[Int] mustBe 3
//      (response.json \ "returnsOverdue").as[Int] mustBe 4
//    }
//
//    "return 200 with correct summary (1,2)" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/XYZ00000000012").futureValue
//      response.status mustBe OK
//      response.contentType mustBe "application/json"
//      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
//      (response.json \ "returnsDue").as[Int] mustBe 1
//      (response.json \ "returnsOverdue").as[Int] mustBe 2
//    }
//
//    "return 200 with correct summary (2,1)" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/XYZ00000000021").futureValue
//      response.status mustBe OK
//      (response.json \ "returnsDue").as[Int] mustBe 2
//      (response.json \ "returnsOverdue").as[Int] mustBe 1
//    }
//
//    "trim whitespace around mgdRegNumber" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/   XYZ00000000010   ").futureValue
//      response.status mustBe OK
//      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000010"
//    }
//
//    "return consistent results across multiple calls" in {
//      AuthStub.authorised()
//      val res1 = get(s"$endpoint/XYZ00000000012").futureValue
//      val res2 = get(s"$endpoint/XYZ00000000012").futureValue
//      res1.json mustBe res2.json
//    }
//
//    "return JSON content type for valid response" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/XYZ00000000012").futureValue
//      response.contentType mustBe "application/json"
//    }
//
//
    "return 400 for partially valid regNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/$GBD/XYZ123?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid regime)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/BAD_REGIME/XYZ00000000012?pageNo=1&pageSize=10").futureValue
      response.status mustBe BAD_REQUEST
    }


//
//    "return 400 for mgdRegNumber with special characters" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/XYZ00000@00000").futureValue
//      response.status mustBe BAD_REQUEST
//    }
//
//    "return 400 for invalid mgdRegNumber format" in {
//      AuthStub.authorised()
//
//      val response = get(s"$endpoint/INVALID").futureValue
//      response.status mustBe BAD_REQUEST
//      (response.json \ "code").as[String] mustBe "INVALID_MGD_REG_NUMBER"
//      (response.json \ "message").as[String] mustBe "mgdRegNumber does not exist"
//    }
//
//    "return 401 when unauthorised" in {
//      AuthStub.unauthorised()
//      val response = get(s"$endpoint/XYZ00000000000").futureValue
//      response.status mustBe UNAUTHORIZED
//    }
//
//    "return 404 for missing mgdRegNumber" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/").futureValue
//      response.status mustBe NOT_FOUND
//    }
//
//    "return 404 for whitespace-only mgdRegNumber" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/   ").futureValue
//      response.status mustBe NOT_FOUND
//    }
//
//    "return 500 when stub simulates failure" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/ERR00000000000").futureValue
//      response.status mustBe INTERNAL_SERVER_ERROR
//      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
//    }
//
//    "return correct error structure for 500 response" in {
//      AuthStub.authorised()
//      val response = get(s"$endpoint/ERR00000000000").futureValue
//      response.status mustBe INTERNAL_SERVER_ERROR
//      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
//      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
//    }

  }
}