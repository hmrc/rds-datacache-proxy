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

class GroupPaymentPeriodInValidRangeRepositorySpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite {

  class GroupPaymentPeriodInValidRangeRepositoryStub extends GroupPaymentPeriodInValidRangeRepository {
    override def getGroupPaymentPeriodInValidRange(gpaUTR: Long,
                                                   nominatedCompanyUTR: Long,
                                                   pPeriod: Long,
                                                   pMonthRestriction: Long
                                                  ): Future[PeriodWithinRange] =
      Future.successful(PeriodWithinRangeHelper.getGroupPaymentPeriodInValidRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GroupPaymentPeriodInValidRangeRepository].toInstance(new GroupPaymentPeriodInValidRangeRepositoryStub))
    .build()

  private lazy val repository: GroupPaymentPeriodInValidRangeRepository = app.injector.instanceOf[GroupPaymentPeriodInValidRangeRepository]

  "getDisplayNeeded" should {

    "return PeriodWithinRange as false" in {
      val result = repository.getGroupPaymentPeriodInValidRange(10L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeFalse
    }

    "return PeriodWithinRange as true" in {
      val result = repository.getGroupPaymentPeriodInValidRange(20L, 1000L, 1L, 1L).futureValue

      result mustBe periodWithinRangeTrue
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getGroupPaymentPeriodInValidRange(999L, 1000L, 1L, 1L).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }

  }

}
