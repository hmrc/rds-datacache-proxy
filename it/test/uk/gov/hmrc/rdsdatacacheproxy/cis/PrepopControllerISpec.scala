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

package uk.gov.hmrc.rdsdatacacheproxy.cis

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class PrepopControllerISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  private val endpoint = "/cis/prepop-subcontractor"

  "POST /cis/prepop-subcontractor" should {

    "return the stubbed trading name" in {
      AuthStub.authorised()

      val res = post(
        endpoint,
        Json.obj(
          "taxOfficeNumber"        -> "123",
          "taxOfficeReference"     -> "AB456",
          "accountOfficeReference" -> "123PA12345678"
        )
      ).futureValue

      res.status mustBe OK
      val subcontractor = (res.json \ "prePopSubcontractors" \ "subcontractors")(0)
      (subcontractor \ "tradingName").asOpt[String] mustBe Some("Test Company Ltd")
    }
  }
}
