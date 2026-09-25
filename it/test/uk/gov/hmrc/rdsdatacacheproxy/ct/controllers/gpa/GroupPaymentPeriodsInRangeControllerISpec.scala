package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.gpa

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PeriodWithinRangeHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.GroupPaymentPeriodsInRangeRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class GroupPaymentPeriodsInRangeControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class GroupPaymentPeriodsInRangeStub extends GroupPaymentPeriodsInRangeRepository {

    override def getGroupPaymentPeriodsInRange(gpaUTR: Long,
                                                   nominatedCompanyUTR: Long,
                                                   pPeriod: Long,
                                                   pMonthRestriction: Long
                                                  ): Future[PeriodWithinRange] = {
      Future.successful(PeriodWithinRangeHelper.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GroupPaymentPeriodsInRangeRepository].toInstance(new GroupPaymentPeriodsInRangeStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /group-payment-periods-in-range" should {

    "return 200 and PeriodWithinRange as false" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/group-payment-periods-in-range/10/1000/1/1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PeriodWithinRange] mustBe periodWithinRangeFalse
    }

    "return 200 and PeriodWithinRange as true" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/group-payment-periods-in-range/20/1000/1/1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PeriodWithinRange] mustBe periodWithinRangeTrue
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/group-payment-periods-in-range/999/1000/1/1").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/group-payment-periods-in-range/10/1000/1/1").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
