/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AccountingPeriods
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AccountingPeriodsRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.AccountingPeriodsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class AccountingPeriodsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class AccountingPeriodsRepositoryStub extends AccountingPeriodsRepository {

    override def getAccountPeriods(taxRef: Long): Future[AccountingPeriods] = {
      Future.successful(AccountingPeriodsStubData.getAccountPeriods(taxRef: Long))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AccountingPeriodsRepository].toInstance(new AccountingPeriodsRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/accounting-periods" should {

    "return 200 and accounting periods with one item" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/accounting-periods/10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AccountingPeriods] mustBe AccountingPeriodsStubData.accountingPeriodsWithOneItem
    }

    "return 200 and accounting periods with multiple items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/accounting-periods/20").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AccountingPeriods] mustBe AccountingPeriodsStubData.accountingPeriodsWithMultipleItems
    }

    "return 200 with empty accounting periods" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/accounting-periods/1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AccountingPeriods] mustBe AccountingPeriodsStubData.emptyAccountingPeriods
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/accounting-periods/200").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/accounting-periods/30").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
