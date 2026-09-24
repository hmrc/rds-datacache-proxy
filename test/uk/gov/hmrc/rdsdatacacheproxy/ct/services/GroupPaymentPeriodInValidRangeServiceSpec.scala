package uk.gov.hmrc.rdsdatacacheproxy.ct.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.GroupPaymentPeriodInValidRangeRepository

import scala.concurrent.Future

class GroupPaymentPeriodInValidRangeServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: GroupPaymentPeriodInValidRangeRepository = mock[GroupPaymentPeriodInValidRangeRepository]

    val service = new GroupPaymentPeriodInValidRangeService(mockRepo)
  }

  "GroupPaymentPeriodInValidRangeServiceSpec" - {
    "must return PeriodWithinRange with field set to false" in new Setup {
      when(mockRepo.getGroupPaymentPeriodInValidRange(any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(periodWithinRangeFalse))

      val result: PeriodWithinRange = service.getGroupPaymentPeriodInValidRange(10L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeFalse

      verify(mockRepo, times(1)).getGroupPaymentPeriodInValidRange(10L, 1000L, 1L, 1L)

    }

    "must return PeriodWithinRange with field set to true" in new Setup {
      when(mockRepo.getGroupPaymentPeriodInValidRange(any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(periodWithinRangeTrue))

      val result: PeriodWithinRange = service.getGroupPaymentPeriodInValidRange(20L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeTrue

      verify(mockRepo, times(1)).getGroupPaymentPeriodInValidRange(20L, 1000L, 1L, 1L)

    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error from downstream")

      when(mockRepo.getGroupPaymentPeriodInValidRange(any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result: Throwable = service.getGroupPaymentPeriodInValidRange(999L, 1000L, 1L, 1L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getGroupPaymentPeriodInValidRange(999L, 1000L, 1L, 1L)

    }
  }

}
