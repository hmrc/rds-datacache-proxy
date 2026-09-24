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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.gpa

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.GroupTaxChargesStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa.GroupTaxChargesService

import scala.concurrent.Future

class GroupTaxChargesControllerSpec extends SpecBase with GroupTaxChargesStubData {

  private trait Setup {
    val mockService: GroupTaxChargesService = mock[GroupTaxChargesService]

    val controller = new GroupTaxChargesController(cc, fakeAuthAction, mockService)

    val pGpaUtr = 19L
    val pGppContractVersion = 12L
    val pStartIndex = 21L
    val pCount = 33L
  }

  "GroupTaxChargesController" - {

    "should return 200 OK with GpaGroupTaxCharges" in new Setup {

      when(mockService.getGpaGroupTaxCharges(any(), any(), any(), any())).thenReturn(Future.successful(gpaWithNonEmptyParticipator))

      val result: Future[Result] = controller.getGpaGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(gpaWithNonEmptyParticipator)

      verify(mockService).getGpaGroupTaxCharges(any(), any(), any(), any())
      verify(mockService, times(1)).getGpaGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount)

    }
    "should return 500 when there is  an exception returned from the service " in new Setup {

      when(mockService.getGpaGroupTaxCharges(any(), any(), any(), any())).thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] = controller.getGpaGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getGpaGroupTaxCharges(any(), any(), any(), any())
      verify(mockService, times(1)).getGpaGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount)
    }
  }

}
