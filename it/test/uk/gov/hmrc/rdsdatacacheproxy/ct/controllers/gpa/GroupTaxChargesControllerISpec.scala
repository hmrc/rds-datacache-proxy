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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.gpa

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.GroupTaxChargesStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.GpaGroupTaxCharges
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.GroupTaxChargesRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GroupTaxChargesControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock with GroupTaxChargesStubData {

  class GroupTaxChargesStub extends GroupTaxChargesRepository {
    override def getGPAGroupTaxCharges(pGpaUtr: Long, pGppContractVersion: Long, pStartIndex: Long, pCount: Long): Future[GpaGroupTaxCharges] =
      Future {
        getGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GroupTaxChargesRepository].toInstance(new GroupTaxChargesStub)
      )
      .build()


   private val endpoint = "/corporation-tax"


  "GET /corporation-tax/group-tax-charges" should {

    "return 200 with GpaGroupTaxCharges" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/group-tax-charges/12/13/14/15").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[GpaGroupTaxCharges] mustBe gpaWithNonEmptyParticipator
    }


    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/group-tax-charges/9798/3786/1/2").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/group-tax-charges/3/12/14/15").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }


}
