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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CompanyDetails, CompanyDetailsResponse}
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.CompanyDetailsService

import scala.concurrent.Future

class CompanyDetailsControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: CompanyDetailsService = mock[CompanyDetailsService]

    val controller = new CompanyDetailsController(cc, fakeAuthAction, mockService)

    val taxPayerReference: Long = 98765L

    val emptyList: CompanyDetailsResponse = CompanyDetailsResponse(taxpayerDetails = List.empty)

    val companyDetailsResponse: CompanyDetailsResponse = CompanyDetailsResponse(
      taxpayerDetails = List(
        CompanyDetails(
          orgUnitId    = "OU-100234",
          companyName  = "Brightwater Engineering Ltd",
          companyRegNo = Some("09876543"),
          addressLine1 = Some("Unit 4, Riverside Park"),
          addressLine2 = Some("Canal Street"),
          addressLine3 = Some("Brierley Hill"),
          addressLine4 = Some("West Midlands"),
          postCode     = Some("DY5 1XY")
        ),
        CompanyDetails(
          orgUnitId    = "OU-100587",
          companyName  = "Northgate Trading Co",
          companyRegNo = Some("123456"),
          addressLine1 = Some("12 Market Square"),
          addressLine2 = Some("12 Square"),
          addressLine3 = Some("address1"),
          addressLine4 = Some("address6"),
          postCode     = Some("B1 2AB")
        )
      )
    )

  }

  "getCompanyDetails" - {

    "should return 200 OK with empty list of CompanyDetailsResponse " in new Setup {

      when(mockService.getCompanyDetails(eqTo(taxPayerReference))).thenReturn(Future.successful(emptyList))

      val result: Future[Result] = controller.getCompanyDetails(taxPayerReference)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(emptyList)

      verify(mockService).getCompanyDetails(taxPayerReference)
      verify(mockService, times(1)).getCompanyDetails(taxPayerReference)

    }
    "should return 200 OK with list CompanyDetailsResponse containing two elements " in new Setup {

      when(mockService.getCompanyDetails(eqTo(taxPayerReference))).thenReturn(Future.successful(companyDetailsResponse))

      val result: Future[Result] = controller.getCompanyDetails(taxPayerReference)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(companyDetailsResponse)

      verify(mockService).getCompanyDetails(taxPayerReference)
      verify(mockService, times(1)).getCompanyDetails(taxPayerReference)

    }
    "should return 500 when there is  an exception returned from the service" in new Setup {

      when(mockService.getCompanyDetails(eqTo(taxPayerReference))).thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] = controller.getCompanyDetails(taxPayerReference)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getCompanyDetails(taxPayerReference)
      verify(mockService, times(1)).getCompanyDetails(taxPayerReference)
    }
  }

}
