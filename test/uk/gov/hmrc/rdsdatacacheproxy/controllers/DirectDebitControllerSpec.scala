/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.controllers

import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.WorkingDaysOffsetRequest
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.EarliestPaymentDate
import uk.gov.hmrc.rdsdatacacheproxy.services.DirectDebitService

import java.time.LocalDate
import scala.concurrent.Future

class DirectDebitControllerSpec extends SpecBase {

  "DirectDebitController" - {
    "getWorkingDaysOffset method" - {
      "return 200 and a successful response when the request is valid" in new SetUp {
        val result: Future[Result] = controller.getWorkingDaysOffset()(fakeRequestWithJsonBody(Json.toJson(testRequestModel)))

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(EarliestPaymentDate(LocalDate.of(2025, 12, 13)))
      }

      "return 400 when the request is not valid" in new SetUp {
        val result: Future[Result] = controller.getWorkingDaysOffset()(fakeRequestWithJsonBody(Json.toJson("invalid json")))

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  class SetUp {
    val mockDirectDebitService: DirectDebitService = mock[DirectDebitService]

    val testRequestModel: WorkingDaysOffsetRequest = WorkingDaysOffsetRequest(LocalDate.of(2025, 12, 5), 8)
    val controller = new DirectDebitController(fakeAuthAction, mockDirectDebitService, cc)
  }
}
