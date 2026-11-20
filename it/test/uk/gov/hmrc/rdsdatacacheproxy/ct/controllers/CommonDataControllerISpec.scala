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
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CommonData
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.CommonDataRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import java.time.LocalDate
import scala.concurrent.Future

class CommonDataControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class CommonDataStub extends CommonDataRepository {
    override def getCommonData(): Future[CommonData] =
      Future.successful(
        CommonData(asOnDate = LocalDate.of(2026, 9, 15))
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[CommonDataRepository].toInstance(new CommonDataStub)
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/common-data" should {

    "return 200 with common data" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/common-data").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[CommonData] mustBe
        CommonData(asOnDate = LocalDate.of(2026, 9, 15))
    }
  }

  "return 401 when unauthorised" in {
    AuthStub.unauthorised()

    val response = get(s"$endpoint/common-data").futureValue

    response.status mustBe UNAUTHORIZED
  }
}
