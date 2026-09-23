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
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.CompanyNominator
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.CompanyNominatorRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.gpa.CompanyNominatorStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class CompanyNominatorControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class CompanyNominatorRepositoryStub extends CompanyNominatorRepository {

    override def getIsCompanyNominatorOfGPA(gpaUtr: Long, nominatedCompanyUtr: Long): Future[CompanyNominator] = {
      Future.successful(CompanyNominatorStubData.getIsCompanyNominatorOfGPA(gpaUtr: Long, nominatedCompanyUtr: Long))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[CompanyNominatorRepository].toInstance(new CompanyNominatorRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/is-company-nominator" should {

    "return 200 and company nominator when isParticipator is 'Y'" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/is-company-nominator/10/2").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[CompanyNominator] mustBe CompanyNominatorStubData.companyNominatorTrue
    }

    "return 200 and company nominator when isParticipator is 'N'" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/is-company-nominator/20/2").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[CompanyNominator] mustBe CompanyNominatorStubData.companyNominatorFalse
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/is-company-nominator/200/3").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/is-company-nominator/30/4").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
