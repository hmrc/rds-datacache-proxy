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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.manageClient

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.*
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient.GetClientsByUtrHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.manageClient.GetClientsByUtrService

import scala.concurrent.Future

class GetClientsByUtrControllerSpec extends SpecBase with MockitoSugar with GetClientsByUtrHelper {

  private class Setup {
    val mockService: GetClientsByUtrService = mock[GetClientsByUtrService]
    val controller: GetClientsByUtrController =
      new GetClientsByUtrController(fakeAuthAction, mockService, cc)
  }

  "GetClientsByUtrControllerSpec" - {

    "return a 200 and a successful response for an empty client list" in new Setup {
      when(mockService.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(emptyClients))

      val result: Future[Result] =
        controller.getClientsByUtr("IR1001", "CR1001", "1234567890")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(emptyClients)

      verify(mockService).getClientsByUtr("IR1001", "CR1001", "1234567890")
    }

    "return a 200 and a successful response for a single client" in new Setup {
      when(mockService.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(singleClient))

      val result: Future[Result] =
        controller.getClientsByUtr("IR2002", "CR2002", "1234567890")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(singleClient)

      verify(mockService).getClientsByUtr("IR2002", "CR2002", "1234567890")
    }

    "return a 200 and a successful response for multiple clients" in new Setup {
      when(mockService.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(multipleClients))

      val result: Future[Result] =
        controller.getClientsByUtr("IR3003", "CR3003", "2345678901")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(multipleClients)

      verify(mockService).getClientsByUtr("IR3003", "CR3003", "2345678901")
    }

    "return a 200 and a successful response for multiple client name starting characters" in new Setup {
      when(mockService.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.successful(multipleNameStartChars))

      val result: Future[Result] =
        controller.getClientsByUtr("IR4004", "CR4004", "4567890123")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(multipleNameStartChars)

      verify(mockService).getClientsByUtr("IR4004", "CR4004", "4567890123")
    }

    "return 500 when the service call fails" in new Setup {
      when(mockService.getClientsByUtr(any[String], any[String], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] =
        controller.getClientsByUtr("IR9999", "CR9999", "9999999999")(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")

      verify(mockService).getClientsByUtr("IR9999", "CR9999", "9999999999")
    }
  }
}
