package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers


import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.GroupPaymentPeriodInValidRangeService
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}

import scala.concurrent.Future

class GroupPaymentPeriodInValidRangeControllerSpec extends SpecBase with MockitoSugar {

  private trait Setup {
    val mockService: GroupPaymentPeriodInValidRangeService = mock[GroupPaymentPeriodInValidRangeService]
    val mockController: GroupPaymentPeriodInValidRangeController = new GroupPaymentPeriodInValidRangeController(fakeAuthAction, mockService, cc)
  }

  "getDisplayNeeded" - {

    "returns 200 with PeriodWithinRange with field set to false" in new Setup {
      val gpaUTR: Long = 10L
      val nominatedCompanyUTR: Long = 1000L
      val pPeriod: Long = 1L
      val pMonthRestriction: Long = 1L

      when(mockService.getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)).thenReturn(Future.successful(periodWithinRangeFalse))

      val result: Future[Result] = mockController.getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(periodWithinRangeFalse)

      verify(mockService).getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
      verify(mockService, times(1)).getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)

    }

    "returns 200 with PeriodWithinRange with field set to true" in new Setup {
      val gpaUTR: Long = 20L
      val nominatedCompanyUTR: Long = 1000L
      val pPeriod: Long = 1L
      val pMonthRestriction: Long = 1L

      when(mockService.getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)).thenReturn(Future.successful(periodWithinRangeTrue))

      val result: Future[Result] = mockController.getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(periodWithinRangeTrue)

      verify(mockService).getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
      verify(mockService, times(1)).getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)

    }

    "returns 500 with generic error message on runtime exception" in new Setup {
      val gpaUTR: Long = 20L
      val nominatedCompanyUTR: Long = 1000L
      val pPeriod: Long = 1L
      val pMonthRestriction: Long = 1L
      
      when(mockService.getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)).thenReturn(Future.failed(new RuntimeException("Error from downstream")))

      val result: Future[Result] = mockController.getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve period within range"

      verify(mockService)getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
      verify(mockService, times(1))getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction)
    }

  }

}
