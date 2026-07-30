package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{PayRepayReallocations, PayRepayReallocationsList}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.PayRepayReallocationRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.PayRepayReallocationStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class PayRepayReallocationControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class PayRepayReallocationRepositoryStub extends PayRepayReallocationRepository {

    override def getTotalAmounts(taxRef: Long, accPeriod: Long): Future[PayRepayReallocationsList] = {
      Future.successful(PayRepayReallocationStubData.getTotalAmounts(taxRef: Long, accPeriod: Long))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PayRepayReallocationRepository].toInstance(new PayRepayReallocationRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/total-amount-payment-repayment-reallocation" should {

    "return 200 with payment repayment reallocation list with two items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/total-amount-payment-repayment-reallocation/10/3").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PayRepayReallocationsList] mustBe PayRepayReallocationsList(PayRepayReallocationsListStubData.getTotalAmounts(10L, 3L))
    }

    "return 200 with payment repayment reallocation empty list" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/total-amount-payment-repayment-reallocation/1/2").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PayRepayReallocationsList] mustBe PayRepayReallocationsList(PayRepayReallocationsListStubData.emptyPayRepayReallocationsList)
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/total-amount-payment-repayment-reallocation/200/3").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/total-amount-payment-repayment-reallocation/30/4").futureValue
      response.status mustBe UNAUTHORIZED
    }
  }

}
