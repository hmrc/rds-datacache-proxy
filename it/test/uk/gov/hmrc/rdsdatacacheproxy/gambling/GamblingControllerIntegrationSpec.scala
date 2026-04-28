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
import play.api.libs.json.Reads
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.MgdCertificate

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingDataSource
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import java.time.LocalDate
import scala.concurrent.Future

class GamblingControllerIntegrationSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class GamblingRdsStub extends GamblingDataSource {

    override def getReturnSummary(mgdRegNumber: String) =
      Future {
        GamblingStubData.getReturnSummary(mgdRegNumber)
      }
    override def getBusinessName(mgdRegNumber: String) =
      Future {
        GamblingStubData.getBusinessName(mgdRegNumber)
      }

    override def getBusinessDetails(mgdRegNumber: String) =
      Future {
        GamblingStubData.getBusinessDetails(mgdRegNumber)
      }

    override def getMgdCertificate(mgdRegNumber: String) =
      Future.successful(
        MgdCertificate(
          mgdRegNumber         = mgdRegNumber,
          registrationDate     = None,
          individualName       = None,
          businessName         = Some("Test Business"),
          tradingName          = None,
          repMemName           = None,
          busAddrLine1         = None,
          busAddrLine2         = None,
          busAddrLine3         = None,
          busAddrLine4         = None,
          busPostcode          = None,
          busCountry           = None,
          busAdi               = None,
          repMemLine1          = None,
          repMemLine2          = None,
          repMemLine3          = None,
          repMemLine4          = None,
          repMemPostcode       = None,
          repMemAdi            = None,
          typeOfBusiness       = None,
          businessTradeClass   = None,
          noOfPartners         = None,
          groupReg             = "N",
          noOfGroupMems        = None,
          dateCertIssued       = None,
          partMembers          = Seq.empty,
          groupMembers         = Seq.empty,
          returnPeriodEndDates = Seq.empty
        )
      )
  }


  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GamblingDataSource].toInstance(new GamblingRdsStub)
      )
      .build()

  private val businessDetailEndpoint = "/gambling/business-details"

  implicit val localDateReads: Reads[LocalDate] =
    Reads.localDateReads("yyyy-MM-dd")

  implicit val optLocalDateReads: Reads[Option[LocalDate]] =
    Reads.optionWithNull[LocalDate]

  private val endpointReturnSummary = "/gambling/return-summary"
  private val endpointBusinessName = "/gambling/business-name"

  "GET /gambling/return-summary (stubbed repo, no DB)" should {

    "return 200 with correct summary (0,0)" in {
      AuthStub.authorised()

      val response = get(s"$endpointReturnSummary/XYZ00000000000").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000000"
      (response.json \ "returnsDue").as[Int] mustBe 0
      (response.json \ "returnsOverdue").as[Int] mustBe 0
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/xyz00000000012 ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
    }

    "return default values for unknown mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/XYZ99999999999").futureValue
      response.status mustBe OK
      (response.json \ "returnsDue").as[Int] mustBe 3
      (response.json \ "returnsOverdue").as[Int] mustBe 4
    }

    "return 200 with correct summary (1,2)" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/XYZ00000000012").futureValue
      response.status mustBe OK
      response.contentType mustBe "application/json"
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
      (response.json \ "returnsDue").as[Int] mustBe 1
      (response.json \ "returnsOverdue").as[Int] mustBe 2
    }

    "return 200 with correct summary (2,1)" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/XYZ00000000021").futureValue
      response.status mustBe OK
      (response.json \ "returnsDue").as[Int] mustBe 2
      (response.json \ "returnsOverdue").as[Int] mustBe 1
    }

    "trim whitespace around mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/   XYZ00000000010   ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000010"
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$endpointReturnSummary/XYZ00000000012").futureValue
      val res2 = get(s"$endpointReturnSummary/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }


    "return 400 for partially valid mgdRegNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/XYZ123").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for mgdRegNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid mgdRegNumber format" in {
      AuthStub.authorised()

      val response = get(s"$endpointReturnSummary/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_MGD_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "mgdRegNumber does not exist"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpointReturnSummary/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$endpointReturnSummary/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }

  }

  "GET /gambling/business-name (stubbed repo, no DB)" should {

    "return 200 with correct summary" in {
      AuthStub.authorised()

      val response = get(s"$endpointBusinessName/XYZ00000000000").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000000"
      (response.json \ "solePropTitle").as[String] mustBe "Mr"
      (response.json \ "solePropFirstName").as[String] mustBe "John"
      (response.json \ "solePropMidName").as[String] mustBe "C"
      (response.json \ "solePropLastName").as[String] mustBe "Doe"
      (response.json \ "businessName").as[String] mustBe "John Doe Co."
      (response.json \ "businessType").as[Int] mustBe 1
      (response.json \ "tradingName").as[String] mustBe "DoeDoe"
      (response.json \ "systemDate").as[LocalDate] mustBe LocalDate.of(2026, 4, 20)
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/xyz00000000012 ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
    }

    "return default values for unknown mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/XYZ99999999999").futureValue
      response.status mustBe OK
      (response.json \ "solePropTitle").as[String] mustBe "Mr"
      (response.json \ "solePropFirstName").as[String] mustBe "Foo"
      (response.json \ "solePropMidName").as[String] mustBe "B"
      (response.json \ "solePropLastName").as[String] mustBe "Bar"
      (response.json \ "businessName").as[String] mustBe "FooBar Co."
      (response.json \ "businessType").as[Int] mustBe 1
      (response.json \ "tradingName").as[String] mustBe "Foobar"
      (response.json \ "systemDate").as[LocalDate] mustBe LocalDate.of(2026, 4, 20)
    }

    "return 200 with correct summary of Miss Catherine Haversham" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/XYZ00000000012").futureValue
      response.status mustBe OK
      response.contentType mustBe "application/json"
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
      (response.json \ "solePropTitle").as[String] mustBe "Miss"
      (response.json \ "solePropFirstName").as[String] mustBe "Catherine"
      (response.json \ "solePropMidName").asOpt[String] mustBe None
      (response.json \ "solePropLastName").as[String] mustBe "Havisham"
      (response.json \ "businessName").as[String] mustBe "Failed Expectations"
      (response.json \ "businessType").as[Int] mustBe 1
      (response.json \ "tradingName").as[String] mustBe "Miss Havisham"
      (response.json \ "systemDate").as[LocalDate] mustBe LocalDate.of(1991, 1, 1)
    }

    "return 200 with correct summary of Mr Eugine H Krabs" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/XYZ00000000021").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000021"
      (response.json \ "solePropTitle").as[String] mustBe "Mr"
      (response.json \ "solePropFirstName").as[String] mustBe "Eugine"
      (response.json \ "solePropMidName").as[String] mustBe "H"
      (response.json \ "solePropLastName").as[String] mustBe "Krabs"
      (response.json \ "businessName").as[String] mustBe "Krusty Krab"
      (response.json \ "businessType").as[Int] mustBe 1
      (response.json \ "tradingName").as[String] mustBe "Mr Krabs"
      (response.json \ "systemDate").as[LocalDate] mustBe LocalDate.of(1991, 1, 1)
    }

    "trim whitespace around mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/   XYZ00000000010   ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000010"
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$endpointBusinessName/XYZ00000000012").futureValue
      val res2 = get(s"$endpointBusinessName/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }


    "return 400 for partially valid mgdRegNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/XYZ123").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for mgdRegNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid mgdRegNumber format" in {
      AuthStub.authorised()

      val response = get(s"$endpointBusinessName/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_MGD_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "mgdRegNumber does not exist"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpointBusinessName/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$endpointBusinessName/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }

  }

  "GET /gambling/business-details (stubbed repo, no DB)" should {

    "return 200 with correct summary (0,0)" in {
      AuthStub.authorised()

      val response = get(s"$businessDetailEndpoint/XYZ00000000000").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000000"
      (response.json \ "businessType").as[Int] mustBe 6
      (response.json \ "currentlyRegistered").as[Int] mustBe 2
      (response.json \ "groupReg").as[String] mustBe "foo"
      (response.json \ "dateOfRegistration").as[Option[LocalDate]] mustBe Some(LocalDate.of(2024, 4, 21))
      (response.json \ "businessPartnerNumber").as[String] mustBe "bar"
      (response.json \ "systemDate").as[Option[LocalDate]] mustBe Some(LocalDate.of(2024, 4, 21))
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/xyz00000000012 ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
    }

    "return default values for unknown mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/XYZ99999999999").futureValue
      response.status mustBe OK
      (response.json \ "businessType").as[Int] mustBe 0
      (response.json \ "currentlyRegistered").as[Int] mustBe 0
      (response.json \ "groupReg").as[String] mustBe "unknown"
      (response.json \ "dateOfRegistration").as[Option[LocalDate]] mustBe Some(LocalDate.of(2026, 4, 22))
      (response.json \ "businessPartnerNumber").as[String] mustBe "unknown"
      (response.json \ "systemDate").as[Option[LocalDate]] mustBe Some(LocalDate.of(2026, 4, 22))

    }

    "return 200 with correct summary (1,2)" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/XYZ00000000012").futureValue
      response.status mustBe OK
      response.contentType mustBe "application/json"
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
      (response.json \ "businessType").as[Int] mustBe 1
      (response.json \ "currentlyRegistered").as[Int] mustBe 2
      (response.json \ "groupReg").as[String] mustBe "foobar"
      (response.json \ "dateOfRegistration").as[Option[LocalDate]] mustBe Some(LocalDate.of(2023, 4, 21))
      (response.json \ "businessPartnerNumber").as[String] mustBe "barfoo"
      (response.json \ "systemDate").as[Option[LocalDate]] mustBe Some(LocalDate.of(2023, 4, 21))

    }

    "return 200 with correct summary (2,1)" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/XYZ00000000021").futureValue
      response.status mustBe OK
      (response.json \ "businessType").as[Int] mustBe 5
      (response.json \ "currentlyRegistered").as[Int] mustBe 2
      (response.json \ "groupReg").as[String] mustBe "foofoo"
      (response.json \ "dateOfRegistration").as[Option[LocalDate]] mustBe Some(LocalDate.of(2024, 1, 21))
      (response.json \ "businessPartnerNumber").as[String] mustBe "barbar"
      (response.json \ "systemDate").as[Option[LocalDate]] mustBe Some(LocalDate.of(2024, 1, 21))

    }

    "trim whitespace around mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/   XYZ00000000010   ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000010"
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$businessDetailEndpoint/XYZ00000000012").futureValue
      val res2 = get(s"$businessDetailEndpoint/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid mgdRegNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/XYZ123").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for mgdRegNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid mgdRegNumber format" in {
      AuthStub.authorised()

      val response = get(s"$businessDetailEndpoint/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_MGD_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "mgdRegNumber does not exist"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$businessDetailEndpoint/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$businessDetailEndpoint/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }

  }
}
