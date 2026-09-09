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
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.FormDataStub
import uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.FormDataQueryParams
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.FormDataRepositoryImpl

import java.time.LocalDate
import scala.concurrent.Future

class FormDateControllerSpec extends SpecBase with MockitoSugar with FormDataStub {

  private class SetUp {
    val mockFormDataRepository: FormDataRepositoryImpl = mock[FormDataRepositoryImpl]
    val controller: FormDataController =
      new FormDataController(fakeAuthAction, mockFormDataRepository, cc)
  }

  "FormDataController#getFormData" - {
    "return 200 and a successful response when repository return default record" in new SetUp {
      when(mockFormDataRepository.getData(any[Long], any[Long], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.successful(defaultDataItem))

      val result: Future[Result] =
        controller.getFormData(1L, 1L, FormDataQueryParams(LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")))(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(defaultDataItem)
      contentType(result)   shouldBe Some("application/json")
      verify(mockFormDataRepository).getData(1L, 1L, LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31"))
    }

    "return 200 and a successful response when repository return empty record" in new SetUp {
      when(mockFormDataRepository.getData(any[Long], any[Long], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.successful(emptyDataItem))

      val result: Future[Result] =
        controller.getFormData(2L, 1L, FormDataQueryParams(LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")))(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(emptyDataItem)
      contentType(result)   shouldBe Some("application/json")
      verify(mockFormDataRepository).getData(2L, 1L, LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31"))
    }

    "return 500 and when repository call fails" in new SetUp {
      when(mockFormDataRepository.getData(any[Long], any[Long], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] =
        controller.getFormData(2L, 1L, FormDataQueryParams(LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")))(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockFormDataRepository).getData(2L, 1L, LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31"))
    }

  }

}
