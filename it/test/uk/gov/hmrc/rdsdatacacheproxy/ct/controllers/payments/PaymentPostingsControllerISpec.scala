package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.payments

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentPostingsResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PaymentPostingsRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PaymentPostingsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future


class PaymentPostingsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock with PaymentPostingsStubData {

  class PaymentPostingsStubData extends PaymentPostingsRepository {
    override def getPaymentPostings(taxRef: Long): Future[PaymentPostingsResponse] = {
      Future.successful(getPaymentPostingsFromStubs(taxRef))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PaymentPostingsRepository].toInstance(new PaymentPostingsStubData())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/payment-postings" should {

    "return 200 and an empty payment posting list" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/payment-postings/124252").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentPostingsResponse] mustBe emptyPaymentPostings
    }

    "return 200 and an single payment posting" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/payment-postings/7293742").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentPostingsResponse] mustBe singlePaymentPosting
    }

    "return 200 and multiple payment posting" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/payment-postings/93837").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentPostingsResponse] mustBe multiplePaymentPostings
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/payment-postings/99999").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/payment-postings/99999").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}