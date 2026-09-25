package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.PeriodWithinRangeHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}

import scala.concurrent.Future

class GroupPaymentPeriodsInRangeRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite {

  class GroupPaymentPeriodsInRangeRepositoryStub extends GroupPaymentPeriodsInRangRepository {
    override def getGroupPaymentPeriodsInRange(gpaUTR: Long,
                                                   nominatedCompanyUTR: Long,
                                                   pPeriod: Long,
                                                   pMonthRestriction: Long
                                                  ): Future[PeriodWithinRange] =
      Future.successful(PeriodWithinRangeHelper.GroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GroupPaymentPeriodsInRangeRepository].toInstance(new GroupPaymentPeriodsInRangeRepositoryStub))
    .build()

  private lazy val repository: GroupPaymentPeriodsInRangeRepository = app.injector.instanceOf[GroupPaymentPeriodsInRangeRepository]

  "getGroupPaymentPeriodsInRange" should {

    "return PeriodWithinRange as false" in {
      val result = repository.getGroupPaymentPeriodsInRange(10L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeFalse
    }

    "return PeriodWithinRange as true" in {
      val result = repository.getGroupPaymentPeriodsInRange(20L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeTrue
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getGroupPaymentPeriodsInRange(999L, 1000L, 1L, 1L).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }

  }

}
