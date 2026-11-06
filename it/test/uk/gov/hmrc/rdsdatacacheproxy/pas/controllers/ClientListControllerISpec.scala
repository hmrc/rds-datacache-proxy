/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.pas.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSResponse
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.ClientListSource
import uk.gov.hmrc.rdsdatacacheproxy.pas.stub.PasStubData

import scala.concurrent.Future

class ClientListControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class ClientListStub extends ClientListSource {
    override def getClientListDownloadStatus(credentialId: String,
                                             serviceName: String,
                                             gracePeriod: Int): Future[Int] =
        PasStubData.getClientListDownloadStatusResponse(credentialId, serviceName, gracePeriod)

    override def hasClient(irAgentId: String,
                           credentialId: String,
                           taxOfficeNumber: String,
                           taxOfficeReference: String,
                           serviceName: String): Future[Boolean] =
        PasStubData.hasClientResponse(irAgentId, credentialId, taxOfficeNumber, taxOfficeReference, serviceName)
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[ClientListSource].toInstance(new ClientListStub)
      )
      .build()

  private val endpoint = "/pas/agent/clientlist/status"

  private def getClientListStatus(credentialId: String, serviceName: String, gracePeriod: Int = 14400): WSResponse =
    get(s"$endpoint?credentialId=$credentialId&serviceName=$serviceName&gracePeriod=$gracePeriod").futureValue

  "GET /pas/agent/clientlist/status (stubbed repo, no DB)" should {

    "return 200 with status 'Succeeded' when authorised and parameters are valid" in {
      AuthStub.authorised()
      val res = getClientListStatus("cred-123", "service-xyz")

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 200 with status 'Succeeded' when using default grace period" in {
      AuthStub.authorised()
      val res = get(s"$endpoint?credentialId=cred-123&serviceName=service-xyz").futureValue

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 400 when credentialId is empty" in {
      AuthStub.authorised()
      val res = getClientListStatus("", "service-xyz")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and serviceName must be provided"
    }

    "return 400 when serviceName is empty" in {
      AuthStub.authorised()
      val res = getClientListStatus("cred-123", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and serviceName must be provided"
    }

    "return 400 when both credentialId and serviceName are empty" in {
      AuthStub.authorised()
      val res = getClientListStatus("", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and serviceName must be provided"
    }

    "return 200 with status 'Succeeded' when serviceName has whitespace" in {
      AuthStub.authorised()
      val res = get(s"$endpoint?credentialId=cred-123&serviceName=%20%20service-xyz%20%20&gracePeriod=14400").futureValue

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 200 with status 'Succeeded' for custom grace period values" in {
      AuthStub.authorised()
      val res1 = getClientListStatus("cred-123", "service-xyz", 7200)
      res1.status mustBe OK
      (res1.json \ "status").as[String] mustBe "Succeeded"

      val res2 = getClientListStatus("cred-123", "service-xyz", 0)
      res2.status mustBe OK
      (res2.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = getClientListStatus("cred-123", "service-xyz")

      res.status mustBe UNAUTHORIZED
    }

    "return 400 when credentialId parameter is missing" in {
      AuthStub.authorised()
      val res = get(s"$endpoint?serviceName=service-xyz&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when serviceName parameter is missing" in {
      AuthStub.authorised()
      val res = get(s"$endpoint?credentialId=cred-123&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when all parameters are missing" in {
      AuthStub.authorised()
      val res = get(endpoint).futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 404 for unknown endpoint (routing sanity)" in {
      AuthStub.authorised()
      val res = get("/client-list-status-does-not-exist?credentialId=cred-123&serviceName=service-xyz").futureValue

      res.status mustBe NOT_FOUND
    }

    "handle special characters in credentialId and serviceName" in {
      AuthStub.authorised()
      val res = get(s"$endpoint?credentialId=cred-123%2Bspecial&serviceName=service-xyz%2Ftest&gracePeriod=14400").futureValue

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 400 when credentialId contains only whitespace" in {
      AuthStub.authorised()
      val res = get(s"$endpoint?credentialId=%20%20%20&serviceName=service-xyz&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and serviceName must be provided"
    }

    "return 400 when serviceName contains only whitespace" in {
      AuthStub.authorised()
      val res = get(s"$endpoint?credentialId=cred-123&serviceName=%20%20%20&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and serviceName must be provided"
    }
  }

  private val hasClientEndpoint = "/pas/agent/clientlist/has-client"
  private val hasClientErrorMessage = "irAgentId, credentialId, taxOfficeNumber, taxOfficeReference and serviceName must be provided"

  private def getHasClient(irAgentId: String,
                           credentialId: String,
                           taxOfficeNumber: String,
                           taxOfficeReference: String,
                           serviceName: String): WSResponse =
    get(
      s"$hasClientEndpoint?irAgentId=$irAgentId&credentialId=$credentialId&taxOfficeNumber=$taxOfficeNumber&taxOfficeReference=$taxOfficeReference&serviceName=$serviceName"
    ).futureValue

  "GET /pas/agent/clientlist/has-client (stubbed repo, no DB)" should {

    "return 200 with hasClient true when authorised and serviceName is CIS" in {
      AuthStub.authorised()
      val res = getHasClient("123456", "CRED01", "754", "EZ00100", "CIS")

      res.status mustBe OK
      (res.json \ "hasClient").as[Boolean] mustBe true
    }

    "return 200 with hasClient true when authorised and serviceName is PAYE" in {
      AuthStub.authorised()
      val res = getHasClient("123456", "CRED01", "754", "EZ00100", "PAYE")

      res.status mustBe OK
      (res.json \ "hasClient").as[Boolean] mustBe true
    }

    "return 400 when irAgentId is empty" in {
      AuthStub.authorised()
      val res = getHasClient("", "CRED01", "754", "EZ00100", "CIS")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe hasClientErrorMessage
    }

    "return 400 when credentialId is empty" in {
      AuthStub.authorised()
      val res = getHasClient("123456", "", "754", "EZ00100", "CIS")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe hasClientErrorMessage
    }

    "return 400 when taxOfficeNumber is empty" in {
      AuthStub.authorised()
      val res = getHasClient("123456", "CRED01", "", "EZ00100", "CIS")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe hasClientErrorMessage
    }

    "return 400 when taxOfficeReference is empty" in {
      AuthStub.authorised()
      val res = getHasClient("123456", "CRED01", "754", "", "CIS")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe hasClientErrorMessage
    }

    "return 400 when serviceName is empty" in {
      AuthStub.authorised()
      val res = getHasClient("123456", "CRED01", "754", "EZ00100", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe hasClientErrorMessage
    }

    "return 400 when irAgentId contains only whitespace" in {
      AuthStub.authorised()
      val res = getHasClient("%20%20%20", "CRED01", "754", "EZ00100", "CIS")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe hasClientErrorMessage
    }

    "return 400 when irAgentId parameter is missing" in {
      AuthStub.authorised()
      val res = get(s"$hasClientEndpoint?credentialId=CRED01&taxOfficeNumber=754&taxOfficeReference=EZ00100&serviceName=CIS").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when serviceName parameter is missing" in {
      AuthStub.authorised()
      val res = get(s"$hasClientEndpoint?irAgentId=123456&credentialId=CRED01&taxOfficeNumber=754&taxOfficeReference=EZ00100").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when all parameters are missing" in {
      AuthStub.authorised()
      val res = get(hasClientEndpoint).futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = getHasClient("123456", "CRED01", "754", "EZ00100", "CIS")

      res.status mustBe UNAUTHORIZED
    }
  }

}
