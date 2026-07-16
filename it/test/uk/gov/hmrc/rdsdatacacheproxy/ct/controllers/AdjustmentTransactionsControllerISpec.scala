package uk.gov.hmrc.rdsdatacacheproxy.ct

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{AdjustmentTransactions, AdjustmentTransactionsList}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AdjustmentTransactionsRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.*
import scala.concurrent.Future

class AdjustmentTransactionsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class AdjustmentTransactionsRepositoryStub extends AdjustmentTransactionsRepository {

    override def getAdjustmentTransactions(taxRef: Long, accPeriod: Long): Future[List[AdjustmentTransactions]] = {
      Future.successful(AdjustmentTransactionsStubData.getAdjustmentTransactions(taxRef: Long, accPeriod: Long))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AdjustmentTransactionsRepository].toInstance(new AdjustmentTransactionsRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/adjustment-transactions" should {

    "return 200 with adjustment transactions list with two items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/adjustment-transactions/10/2").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AdjustmentTransactionsList] mustBe AdjustmentTransactionsList(AdjustmentTransactionsStubData.getAdjustmentTransactions(10L, 3L))
    }

    "return 200 with adjustment transactions empty list" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/adjustment-transactions/1/2").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AdjustmentTransactionsList] mustBe AdjustmentTransactionsList(adjustmentTransactionsList = List[AdjustmentTransactions]())
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/adjustment-transactions/200/3").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/adjustment-transactions/30/4").futureValue
      response.status mustBe UNAUTHORIZED
    }
  }

}
