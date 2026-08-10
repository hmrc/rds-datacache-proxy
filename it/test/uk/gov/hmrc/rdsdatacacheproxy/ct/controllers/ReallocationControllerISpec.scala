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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{ReallocationRow, Reallocations}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.ReallocationDatacacheRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.CorporationTaxStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.CorporationTaxStubData.*
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class ReallocationControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class ReallocationRdsStub extends ReallocationDatacacheRepository {

    def getByAccountingPeriod(taxRef: Long, accPeriod: Long): Future[Seq[ReallocationRow]] = {
      Future.successful(
        getReallocations(taxRef, accPeriod)
      )
    }

  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[ReallocationDatacacheRepository].toInstance(new ReallocationRdsStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET ~/reallocation-to-accounting-period" should {

    "return 200 with empty list of Reallocations" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/reallocation-to-accounting-period/0/3").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Reallocations] mustBe Reallocations(reallocation = reallocationsEmpty)
    }

    "return 200 with list of two Reallocation items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/reallocation-to-accounting-period/2/1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Reallocations] mustBe Reallocations(reallocation = reallocationsTwoItems)
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/reallocation-to-accounting-period/19/1").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/reallocation-to-accounting-period/2/1").futureValue
      response.status mustBe UNAUTHORIZED
    }

  }

}
