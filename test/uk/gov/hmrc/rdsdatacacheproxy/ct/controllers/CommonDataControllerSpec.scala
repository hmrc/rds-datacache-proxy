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

import org.mockito.Mockito.{verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CommonData
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.CommonDataService

import java.time.LocalDate
import scala.concurrent.Future

class CommonDataControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: CommonDataService = mock[CommonDataService]

    val controller = new CommonDataController(fakeAuthAction, mockService, cc)

    val commonData: CommonData = CommonData(
      asOnDate = LocalDate.of(2026, 9, 15)
    )
  }

  "getCommonData" - {

    "should return 200 OK with common data" in new Setup {

      when(mockService.getCommonData())
        .thenReturn(Future.successful(commonData))

      val result: Future[Result] =
        controller.getCommonData().apply(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(commonData)

      verify(mockService).getCommonData()
    }

    "should return 500 when there is an exception returned from the service" in new Setup {

      when(mockService.getCommonData())
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val result: Future[Result] =
        controller.getCommonData().apply(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getCommonData()
    }
  }
}
