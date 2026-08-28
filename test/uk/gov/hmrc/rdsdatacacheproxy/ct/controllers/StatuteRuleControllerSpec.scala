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
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.StatuteRuleRepositoryImpl
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.StatuteRuleDataStub

import scala.concurrent.Future
import java.time.LocalDate
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.StatuteRule
import uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.StatuteQueryParams

class StatuteRuleControllerSpec extends SpecBase with MockitoSugar with StatuteRuleDataStub {

  private class SetUp {
    val mockStatuteRuleRepository: StatuteRuleRepositoryImpl = mock[StatuteRuleRepositoryImpl]
    val controller: StatuteRuleController = new StatuteRuleController(fakeAuthAction, mockStatuteRuleRepository, cc)
  }

  "StatuteRuleController#getStatuteRule" - {

    "return a 200:successful response when repository return default record" in new SetUp {
      when(mockStatuteRuleRepository.getStatuteRule(any[String], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.successful(Some(defaultRecord)))

      val result: Future[Result] = controller.getStatuteRule(
        StatuteQueryParams("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09"))
      )(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(StatuteRule(Some(defaultRecord)))
      contentType(result)   shouldBe Some("application/json")
      verify(mockStatuteRuleRepository).getStatuteRule("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09"))
    }

    "return a 200:successful response when repository return empty record" in new SetUp {
      when(mockStatuteRuleRepository.getStatuteRule(any[String], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.successful(Some(recordWithEmptyFields)))

      val result: Future[Result] = controller.getStatuteRule(
        StatuteQueryParams("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09"))
      )(fakeRequest)

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(StatuteRule(Some(recordWithEmptyFields)))
      contentType(result)   shouldBe Some("application/json")
      verify(mockStatuteRuleRepository).getStatuteRule("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09"))
    }

    "return 500 and when repository call fails" in new SetUp {
      when(mockStatuteRuleRepository.getStatuteRule(any[String], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getStatuteRule(
        StatuteQueryParams("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09"))
      )(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockStatuteRuleRepository).getStatuteRule("C", LocalDate.parse("1991-01-01"), LocalDate.parse("1991-06-09"))
    }

  }

}
