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
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.JSON
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{TaxTransactions, TaxTransactionsItem}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.TaxTransactionsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.CorporationTaxStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class TaxTransactionsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class CorporationTaxDataSource extends TaxTransactionsDataSource {
    override def getTaxTransactions(taxRef: Long, accPeriod: Long): Future[List[TaxTransactionsItem]] = {
        Future.successful(CorporationTaxStubData.getTaxTransactions(taxRef, accPeriod))      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[TaxTransactionsDataSource].toInstance(new CorporationTaxDataSource)
      )
      .build()

  private final val endpoint = "/corporation-tax/tax-transactions"
  private final val taxRef1: Long = 1
  private final val taxRef2: Long = 2
  private final val accPeriod1: Long = 1

  "GET /corporation-tax/tax-transactions (stubbed repo, no DB)" should {

    "return 200 with correct Tax Transactions data" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/1/1").futureValue

      response.status mustBe OK
      response.contentType mustBe JSON
      response.json.as[TaxTransactions] mustBe TaxTransactions(CorporationTaxStubData.getTaxTransactions(taxRef1, accPeriod1))
    }

    "return 200 with correct Tax Transactions data for empty list" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/2/1").futureValue

      response.status mustBe OK
      response.contentType mustBe JSON
      response.json.as[TaxTransactions] mustBe TaxTransactions(CorporationTaxStubData.getTaxTransactions(taxRef2, accPeriod1))
    }
    "return 500 with when a downstream error occurs" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/99/1").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/99/1").futureValue
      response.status mustBe UNAUTHORIZED
    }

  }
}
