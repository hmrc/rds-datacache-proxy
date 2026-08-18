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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestAccrual}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.InterestAccrualListDatacacheRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.InterestAccrualListStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class InterestAccrualListIControllerSpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class InterestAccrualListStub extends InterestAccrualListDatacacheRepository {
    override def getInterestAccrualList(taxRef: Long, accPeriod: Long, interestType: String): Future[InterestAccruals] = {
      Future.successful(InterestAccrualListStubData.getAccrualInterestListItems(taxRef, accPeriod, interestType))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[InterestAccrualListDatacacheRepository].toInstance(new InterestAccrualListStub)
      )
      .build()

  private val endpoint = "/corporation-tax"
  private final val taxRef1: Long = 1
  private final val accPeriod1: Long = 1
  private final val interestType: String = "IDB";

  "GET /corporation-tax/interest-accrual-list" should {

    "return 200 with InterestAccrual list contains two items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/interest-accrual-list/1/2/IDE").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[InterestAccruals] mustBe InterestAccrualListStubData.getAccrualInterestListItems(taxRef1, accPeriod1, interestType)
    }

    "return 200 with InterestAccrual empty list" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/interest-accrual-list/19/2/IDE").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[InterestAccruals] mustBe InterestAccrualListStubData.getAccrualInterestListItems(19L, accPeriod1, interestType)
    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/interest-accrual-list/99/99/NON").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/interest-accrual-list/1/2/IDE").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
