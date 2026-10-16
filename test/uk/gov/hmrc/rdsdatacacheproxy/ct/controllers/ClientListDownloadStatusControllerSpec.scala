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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ClientDownloadStatus
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.ClientListDownloadStatusService

import scala.concurrent.Future

class ClientListDownloadStatusControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: ClientListDownloadStatusService = mock[ClientListDownloadStatusService]

    val controller = new ClientListDownloadStatusController(fakeAuthAction, mockService, cc)

    val credentialId: String = "1"
    val serviceName: String = "CT"
    val gracePeriod: Int = 14400

    val initiateDownload: Int = -1
    val inProgress: Int = 0
    val succeeded: Int = 1
    val failed: Int = 2

  }

  "getClientListDownloadStatus" - {

    "should return 200 OK with ClientDownloadStatus" in new Setup {

      when(mockService.getClientListDownloadStatus(eqTo(credentialId), any[String], any[Int])).thenReturn(Future.successful(initiateDownload))

      val result: Future[Result] = controller.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(ClientDownloadStatus(initiateDownload))

      verify(mockService).getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
      verify(mockService, times(1)).getClientListDownloadStatus(credentialId, serviceName, gracePeriod)

    }

    "should return 500 when there is an exception returned from the service" in new Setup {

      when(mockService.getClientListDownloadStatus(eqTo(credentialId), any[String], any[Int])).thenReturn(Future.failed(new RuntimeException("Boom")))

      val result: Future[Result] = controller.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
      verify(mockService, times(1)).getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
    }
  }

}
