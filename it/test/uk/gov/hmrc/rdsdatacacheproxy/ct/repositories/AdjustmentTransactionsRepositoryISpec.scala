package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.bind
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdjustmentTransactions
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.AdjustmentTransactionsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class AdjustmentTransactionsRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class AdjustmentTransactionsRepositoryStub extends AdjustmentTransactionsRepository {

    override def getAdjustmentTransactions(taxRef: Long, accPeriod: Long): Future[List[AdjustmentTransactions]] =
      Future.successful(AdjustmentTransactionsStubData.getAdjustmentTransactions(taxRef: Long, accPeriod: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AdjustmentTransactionsRepository].toInstance(new AdjustmentTransactionsRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[AdjustmentTransactionsRepository]

  "getAdjustmentTransactions" should {

    "return adjustment transactions containing 3 items" in {

      val result = repo.getAdjustmentTransactions(1L, 2L).futureValue

      result mustBe AdjustmentTransactionsStubData.getAdjustmentTransactions(1L, 2L)
    }

    "return empty adjustment transactions" in {

      val result = repo.getAdjustmentTransactions(2L, 3L).futureValue

      result mustBe AdjustmentTransactionsStubData.emptyAdjustmentTransactions

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repo.getAdjustmentTransactions(200L, 2L).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
