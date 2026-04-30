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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{BusinessDetails, GamblingStubData, GetOperatorDetails, MgdCertificate}

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingDataSource
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class GamblingControllerIntegrationSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class GamblingRdsStub extends GamblingDataSource {

    override def getBusinessDetails(mgdRegNumber: String): Future[BusinessDetails] =
      Future.successful(
        uk.gov.hmrc.rdsdatacacheproxy.gambling.models.BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = None,
          currentlyRegistered   = 1,
          isGroupMember         = true,
          dateOfRegistration    = None,
          businessPartnerNumber = Some("BP123"),
          systemDate            = java.time.LocalDate.now()
        )
      )

    override def getOperatorDetails(mgdRegNumber: String): Future[GetOperatorDetails] =
      Future.successful(
        uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GetOperatorDetails(
          mgdRegNumber       = mgdRegNumber,
          solePropName       = None,
          solePropTitle      = None,
          solePropFirstName  = None,
          solePropMiddleName = None,
          solePropLastName   = None,
          tradingName        = Some("Trading Ltd"),
          businessName       = Some("Test Business"),
          businessType       = Some(2),
          adi                = None,
          address1           = None,
          address2           = None,
          address3           = None,
          address4           = None,
          postcode           = None,
          country            = None,
          abroadSig          = None,
          agentOwnRef        = None,
          systemDate         = None
        )
      )

    override def getReturnSummary(mgdRegNumber: String) =
      Future {
        GamblingStubData.getReturnSummary(mgdRegNumber)
      }

    override def getMgdCertificate(mgdRegNumber: String): Future[MgdCertificate] =
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

  private val endpoint = "/gambling/return-summary"

  "GET /gambling/return-summary (stubbed repo, no DB)" should {

    "return 200 with correct summary (0,0)" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/XYZ00000000000").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000000"
      (response.json \ "returnsDue").as[Int] mustBe 0
      (response.json \ "returnsOverdue").as[Int] mustBe 0
    }

    "normalise lowercase input" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/xyz00000000012 ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
    }

    "return default values for unknown mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ99999999999").futureValue
      response.status mustBe OK
      (response.json \ "returnsDue").as[Int] mustBe 3
      (response.json \ "returnsOverdue").as[Int] mustBe 4
    }

    "return 200 with correct summary (1,2)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ00000000012").futureValue
      response.status mustBe OK
      response.contentType mustBe "application/json"
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
      (response.json \ "returnsDue").as[Int] mustBe 1
      (response.json \ "returnsOverdue").as[Int] mustBe 2
    }

    "return 200 with correct summary (2,1)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ00000000021").futureValue
      response.status mustBe OK
      (response.json \ "returnsDue").as[Int] mustBe 2
      (response.json \ "returnsOverdue").as[Int] mustBe 1
    }

    "trim whitespace around mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/   XYZ00000000010   ").futureValue
      response.status mustBe OK
      (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000010"
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = get(s"$endpoint/XYZ00000000012").futureValue
      val res2 = get(s"$endpoint/XYZ00000000012").futureValue
      res1.json mustBe res2.json
    }

    "return JSON content type for valid response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ00000000012").futureValue
      response.contentType mustBe "application/json"
    }

    "return 400 for partially valid mgdRegNumber (wrong length)" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ123").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for mgdRegNumber with special characters" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/XYZ00000@00000").futureValue
      response.status mustBe BAD_REQUEST
    }

    "return 400 for invalid mgdRegNumber format" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/INVALID").futureValue
      response.status mustBe BAD_REQUEST
      (response.json \ "code").as[String] mustBe "INVALID_MGD_REG_NUMBER"
      (response.json \ "message").as[String] mustBe "mgdRegNumber does not exist"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/XYZ00000000000").futureValue
      response.status mustBe UNAUTHORIZED
    }

    "return 404 for missing mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 404 for whitespace-only mgdRegNumber" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/   ").futureValue
      response.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }

    "return correct error structure for 500 response" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/ERR00000000000").futureValue
      response.status mustBe INTERNAL_SERVER_ERROR
      (response.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
      (response.json \ "message").as[String] mustBe "Unexpected error occurred"
    }

    "GET /gambling/business-details/:mgdRegNumber" should {

      val endpoint = "/gambling/business-details"

      "return 200 with business details" in {
        AuthStub.authorised()

        val response = get(s"$endpoint/XYZ00000000012").futureValue

        response.status mustBe OK
        response.contentType mustBe "application/json"

        (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
        (response.json \ "currentlyRegistered").as[Int] mustBe 1
        (response.json \ "isGroupMember").as[Boolean] mustBe true
        (response.json \ "businessPartnerNumber").as[String] mustBe "BP123"
      }

      "return 401 when unauthorised" in {
        AuthStub.unauthorised()

        val response = get(s"$endpoint/XYZ00000000012").futureValue

        response.status mustBe UNAUTHORIZED
      }
    }

    "GET /gambling/operator-details/:mgdRegNumber" should {

      val endpoint = "/gambling/operator-details"

      "return 200 with operator details" in {
        AuthStub.authorised()

        val response = get(s"$endpoint/XYZ00000000012").futureValue

        response.status mustBe OK
        response.contentType mustBe "application/json"

        (response.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
        (response.json \ "businessName").as[String] mustBe "Test Business"
        (response.json \ "tradingName").as[String] mustBe "Trading Ltd"
      }

      "return 401 when unauthorised" in {
        AuthStub.unauthorised()

        val response = get(s"$endpoint/XYZ00000000012").futureValue

        response.status mustBe UNAUTHORIZED
      }
    }

  }
}
