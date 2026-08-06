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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AdminRuleHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.AdministrativeRuleService

import scala.concurrent.Future

class GetAdministrativeRuleControllerSpec extends SpecBase with MockitoSugar with AdminRuleHelper {

  private trait Setup {
    val mockService: AdministrativeRuleService = mock[AdministrativeRuleService]
    val adminRuleKey: String = "INST-PERIOD"
    val controller: GetAdministrativeRuleController = new GetAdministrativeRuleController(cc, fakeAuthAction, mockService)
  }

  "getAdministrativeRule" - {
    "returns 200 OK and empty adminRule when service is invoked" in new Setup {
      when(mockService.getAdminRule(any())).thenReturn(Future.successful(adminRuleWithEmptyFields))

      val result: Future[Result] = controller.getAdministrativeRule(adminRuleKey)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(adminRuleWithEmptyFields)

      verify(mockService).getAdminRule(adminRuleKey)
      verify(mockService, times(1)).getAdminRule(adminRuleKey)

    }
    "returns 200 OK and  adminRule consisting both ruleNumber and ruleDate when service is invoked" in new Setup {
      when(mockService.getAdminRule(any())).thenReturn(Future.successful(adminRuleWithAllFields))

      val result: Future[Result] = controller.getAdministrativeRule(adminRuleKey)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(adminRuleWithAllFields)

      verify(mockService).getAdminRule(adminRuleKey)
      verify(mockService, times(1)).getAdminRule(adminRuleKey)
    }
    "returns 200 OK and adminRule consisting only ruleNumber when service is invoked" in new Setup {
      when(mockService.getAdminRule(any())).thenReturn(Future.successful(adminRuleWithoutRuleDate))

      val result: Future[Result] = controller.getAdministrativeRule(adminRuleKey)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(adminRuleWithoutRuleDate)

      verify(mockService).getAdminRule(adminRuleKey)
      verify(mockService, times(1)).getAdminRule(adminRuleKey)
    }
    "returns 200 OK and adminRule consisting only ruleDate when service is invoked" in new Setup {
      when(mockService.getAdminRule(any())).thenReturn(Future.successful(adminRuleWithoutRuleNumber))

      val result: Future[Result] = controller.getAdministrativeRule(adminRuleKey)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(adminRuleWithoutRuleNumber)

      verify(mockService).getAdminRule(adminRuleKey)
      verify(mockService, times(1)).getAdminRule(adminRuleKey)
    }
    "returns 500 when when the service call fails with an exception " in new Setup {
      val exception = new RuntimeException("Boom")

      when(mockService.getAdminRule(any())).thenReturn(Future.failed(exception))

      val result: Future[Result] = controller.getAdministrativeRule(adminRuleKey)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] shouldBe "Unable to Retrieve adminRule"

      verify(mockService).getAdminRule(adminRuleKey)
      verify(mockService, times(1)).getAdminRule(adminRuleKey)
    }
  }

}
