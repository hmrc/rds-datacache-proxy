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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.DisplayNeededItem
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.DisplayNeededRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.DisplayNeededStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class DisplayNeededControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class DisplayNeededRepositoryStub extends DisplayNeededRepository {

    override def getDisplayNeeded(taxRef: Long, accPeriod: Long): Future[DisplayNeededItem] = {
      Future.successful(DisplayNeededStubData.getDisplayNeeded(taxRef: Long, accPeriod: Long))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[DisplayNeededRepository].toInstance(new DisplayNeededRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/display-needed" should {

    "return 200 and display needed with all flags set as false" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/display-needed/10/1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[DisplayNeededItem] mustBe DisplayNeededStubData.displayNeededItemAllFalse
    }

    "return 200 and display needed with all flags set as true" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/display-needed/20/1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[DisplayNeededItem] mustBe DisplayNeededStubData.displayNeededItemAllTrue
    }

    "return 200 and display needed with some flags set as true and false" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/display-needed/30/1").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[DisplayNeededItem] mustBe DisplayNeededStubData.displayNeededItemMixed
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/display-needed/999/1").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/display-needed/999/1").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
