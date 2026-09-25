package uk.gov.hmrc.rdsdatacacheproxy.ct.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.GroupPaymentPeriodsInRangeRepository

import scala.concurrent.Future

class GroupPaymentPeriodsInRangeServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: GroupPaymentPeriodsInRangeRepository = mock[GroupPaymentPeriodsInRangeRepository]

    val service = new GroupPaymentPeriodsInRangeService(mockRepo)
  }

  "GroupPaymentPeriodsInRangeServiceSpec" - {
    "must return PeriodWithinRange with field set to false" in new Setup {
      when(mockRepo.getGroupPaymentPeriodsInRange(any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(periodWithinRangeFalse))

      val result: PeriodWithinRange = service.getGroupPaymentPeriodsInRange(10L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeFalse

      verify(mockRepo, times(1)).getGroupPaymentPeriodsInRange(10L, 1000L, 1L, 1L)

    }

    "must return PeriodWithinRange with field set to true" in new Setup {
      when(mockRepo.getGroupPaymentPeriodsInRange(any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.successful(periodWithinRangeTrue))

      val result: PeriodWithinRange = service.getGroupPaymentPeriodsInRange(20L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeTrue

      verify(mockRepo, times(1)).getGroupPaymentPeriodsInRange(20L, 1000L, 1L, 1L)

    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error from downstream")

      when(mockRepo.getGroupPaymentPeriodsInRange(any[Long], any[Long], any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result: Throwable = service.getGroupPaymentPeriodsInRange(999L, 1000L, 1L, 1L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getGroupPaymentPeriodsInRange(999L, 1000L, 1L, 1L)

    }
  }

}
