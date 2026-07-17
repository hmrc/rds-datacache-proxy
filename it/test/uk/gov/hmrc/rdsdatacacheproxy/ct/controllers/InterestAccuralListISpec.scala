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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestAccural, InterestAccuralList}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.InterestAccuralListDatacacheRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.InterestAccuralListStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InterestAccuralListISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class InterestAccuralListStub extends InterestAccuralListDatacacheRepository {
    override def getInterestAccuralList(taxRef: Long, accPeriod: Long, interestType: String): Future[List[InterestAccural]] =
      Future {
        InterestAccuralListStubData.getAccuralInterestListItems(taxRef, accPeriod, interestType)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[InterestAccuralListDatacacheRepository].toInstance(new InterestAccuralListStub)
      )
      .build()


   private val endpoint = "/corporation-tax"


  "GET /corporation-tax/interest-accural-list" should {

    "return 200 with InterestAccural list contains two items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/interest-accural-list/1/2/IDE").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[List[InterestAccural]] mustBe InterestAccuralListStubData.interestAccuralListItems
    }

    "return 200 with InterestAccural empty list" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/interest-accural-list/19/2/IDE").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[List[InterestAccural]] mustBe InterestAccuralListStubData.interestAccuralListEmpty

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/interest-accural-list/99/99/NON").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/interest-accural-list/1/2/IDE").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }


}
