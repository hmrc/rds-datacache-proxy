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

import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa.GroupPaymentPeriodsInRangeService

import scala.concurrent.Future

class GroupPaymentPeriodsInRangeControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: GroupPaymentPeriodsInRangeService = mock[GroupPaymentPeriodsInRangeService]
    val mockController: GroupPaymentPeriodsInRangeController = new GroupPaymentPeriodsInRangeController(fakeAuthAction, mockService, cc)
  }

  "getDisplayNeeded" - {

    "returns 200 with PeriodWithinRange with field set to false" in new Setup {
      val gpaUTR: Long = 10L
      val nominatedCompanyUTR: Long = 1000L
      val pPeriod: Long = 1L
      val pMonthRestriction: Long = 1L

      when(mockService.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction))
        .thenReturn(Future.successful(periodWithinRangeFalse))

      val result: Future[Result] = mockController.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(periodWithinRangeFalse)

      verify(mockService).getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
      verify(mockService, times(1)).getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)

    }

    "returns 200 with PeriodWithinRange with field set to true" in new Setup {
      val gpaUTR: Long = 20L
      val nominatedCompanyUTR: Long = 1000L
      val pPeriod: Long = 1L
      val pMonthRestriction: Long = 1L

      when(mockService.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction))
        .thenReturn(Future.successful(periodWithinRangeTrue))

      val result: Future[Result] = mockController.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(periodWithinRangeTrue)

      verify(mockService).getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
      verify(mockService, times(1)).getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)

    }

    "returns 500 with generic error message on runtime exception" in new Setup {
      val gpaUTR: Long = 20L
      val nominatedCompanyUTR: Long = 1000L
      val pPeriod: Long = 1L
      val pMonthRestriction: Long = 1L

      when(mockService.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction))
        .thenReturn(Future.failed(new RuntimeException("Error from downstream")))

      val result: Future[Result] = mockController.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve period within range"

      verify(mockService) getGroupPaymentPeriodsInRange (gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
      verify(mockService, times(1)) getGroupPaymentPeriodsInRange (gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
    }

  }

}
