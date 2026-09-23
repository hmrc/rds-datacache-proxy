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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.AgentClientListResponse
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class AgentControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  private val clientListStatusEndpoint = "/gambling/agent/client-list-status"

  private def getClientListStatus(credentialId: String, regime: String, gracePeriod: Int = 14400): WSResponse =
    get(s"$clientListStatusEndpoint?credentialId=$credentialId&regime=$regime&gracePeriod=$gracePeriod").futureValue

  "GET /client-list-status (stubbed repo, no DB)" should {

    "return 200 with status 'Succeeded' when authorised and parameters are valid" in {
      AuthStub.authorisedAgent()
      val res = getClientListStatus("cred-123", "regime-xyz")

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 200 with status 'Succeeded' when using default grace period" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListStatusEndpoint?credentialId=cred-123&regime=regime-xyz").futureValue

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 400 when credentialId is empty" in {
      AuthStub.authorisedAgent()
      val res = getClientListStatus("", "regime-xyz")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and regime must be provided"
    }

    "return 400 when regime is empty" in {
      AuthStub.authorisedAgent()
      val res = getClientListStatus("cred-123", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and regime must be provided"
    }

    "return 400 when both credentialId and regime are empty" in {
      AuthStub.authorisedAgent()
      val res = getClientListStatus("", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and regime must be provided"
    }

    "return 200 with status 'Succeeded' when regime has whitespace" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListStatusEndpoint?credentialId=cred-123&regime=%20%20regime-xyz%20%20&gracePeriod=14400").futureValue

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 200 with status 'Succeeded' for custom grace period values" in {
      AuthStub.authorisedAgent()
      val res1 = getClientListStatus("cred-123", "regime-xyz", 7200)
      res1.status mustBe OK
      (res1.json \ "status").as[String] mustBe "Succeeded"

      val res2 = getClientListStatus("cred-123", "regime-xyz", 0)
      res2.status mustBe OK
      (res2.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = getClientListStatus("cred-123", "regime-xyz")

      res.status mustBe UNAUTHORIZED
    }

    "return 400 when credentialId parameter is missing" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListStatusEndpoint?regime=regime-xyz&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when regime parameter is missing" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListStatusEndpoint?credentialId=cred-123&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when all parameters are missing" in {
      AuthStub.authorisedAgent()
      val res = get(clientListStatusEndpoint).futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 404 for unknown clientListStatusEndpoint (routing sanity)" in {
      AuthStub.authorisedAgent()
      val res = get("/client-list-status-does-not-exist?credentialId=cred-123&regime=regime-xyz").futureValue

      res.status mustBe NOT_FOUND
    }

    "handle special characters in credentialId and regime" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListStatusEndpoint?credentialId=cred-123%2Bspecial&regime=regime-xyz%2Ftest&gracePeriod=14400").futureValue

      res.status mustBe OK
      (res.json \ "status").as[String] mustBe "Succeeded"
    }

    "return 400 when credentialId contains only whitespace" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListStatusEndpoint?credentialId=%20%20%20&regime=regime-xyz&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and regime must be provided"
    }

    "return 400 when regime contains only whitespace" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListStatusEndpoint?credentialId=cred-123&regime=%20%20%20&gracePeriod=14400").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and regime must be provided"
    }
  }

  private val clientListEndpoint = "/gambling/agent/client-list"

  private def getClientList(
    credentialId: String,
    regime: String,
    start: Int = 0,
    count: Int = -1,
    sort: Int = 0,
    ascending: Boolean = true
  ): WSResponse = {
    val url = s"$clientListEndpoint?credentialId=$credentialId&regime=$regime&start=$start&count=$count&sort=$sort&ascending=$ascending"
    get(url).futureValue
  }

  "GET /client-list (stubbed repo, no DB)" should {

    "return 200 with client list when authorised and parameters are valid" in {
      AuthStub.authorisedAgent()
      val res = getClientList("CRED-ABC-123", "MGD")

      res.status mustBe OK
      val result = res.json.as[AgentClientListResponse]
      result.clients must not be empty
      result.clients.length mustBe 3
      result.totalCount mustBe 3
      result.clientNameStartingCharacters must contain allOf ("J", "S", "X")
    }

    "return 200 with correct client structure" in {
      AuthStub.authorisedAgent()
      val res = getClientList("CRED-ABC-123", "MGD")

      res.status mustBe OK
      val result = res.json.as[AgentClientListResponse]
      
      val firstClient = result.clients.head
      firstClient.regNumber mustBe "111222333"
      firstClient.clientName mustBe "Jones Motors Ltd"
      firstClient.agentOwnRef mustBe "123"
    }

    "return 200 when using default parameters" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListEndpoint?credentialId=CRED-ABC-123&regime=MGD").futureValue

      res.status mustBe OK
      val result = res.json.as[AgentClientListResponse]
      result.clients must not be empty
    }

    "handle pagination parameters correctly" in {
      AuthStub.authorisedAgent()
      val res = getClientList("CRED-ABC-123", "MGD", start = 0, count = 10)

      res.status mustBe OK
      val result = res.json.as[AgentClientListResponse]
      result.clients.length mustBe 3
      result.totalCount mustBe 3
    }

    "handle sort parameter correctly" in {
      AuthStub.authorisedAgent()
      val res1 = getClientList("CRED-ABC-123", "MGD", sort = 0)
      res1.status mustBe OK

      val res2 = getClientList("CRED-ABC-123", "MGD", sort = 1)
      res2.status mustBe OK

      val res3 = getClientList("CRED-ABC-123", "MGD", sort = 2)
      res3.status mustBe OK
    }

    "handle ascending parameter correctly" in {
      AuthStub.authorisedAgent()
      val resAsc = getClientList("CRED-ABC-123", "MGD", ascending = true)
      resAsc.status mustBe OK

      val resDesc = getClientList("CRED-ABC-123", "MGD", ascending = false)
      resDesc.status mustBe OK
    }

    "return 400 when regime is empty" in {
      AuthStub.authorisedAgent()
      val res = getClientList("CRED-ABC-123", " ")

      res.status mustBe BAD_REQUEST
      (res.json \ "message").as[String] mustBe "Invalid Regime Code"
    }

    "return 400 when credentialId is empty" in {
      AuthStub.authorisedAgent()
      val res = getClientList("", "MGD")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId must be provided"
    }

    "return 400 when both irAgentId and credentialId are empty" in {
      AuthStub.authorisedAgent()
      val res = getClientList("", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId must be provided"
    }

    "return 400 when regime contains only whitespace" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListEndpoint?credentialId=CRED-ABC-123&regime=%20%20%20").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "message").as[String] mustBe "Invalid Regime Code"
    }

    "return 400 when credentialId contains only whitespace" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListEndpoint?credentialId=%20%20%20&regime=MGD").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId must be provided"
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = getClientList("CRED-ABC-123", "MGD")

      res.status mustBe UNAUTHORIZED
    }

    "return 400 when irAgentId parameter is missing" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListEndpoint?credentialId=CRED-ABC-123").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when credentialId parameter is missing" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListEndpoint?regime=MGD").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when all parameters are missing" in {
      AuthStub.authorisedAgent()
      val res = get(clientListEndpoint).futureValue

      res.status mustBe BAD_REQUEST
    }

    "handle special characters in credentialId" in {
      AuthStub.authorisedAgent()
      val res = get(s"$clientListEndpoint?credentialId=CRED-ABC-123%2Bspecial&regime=MGD").futureValue

      res.status mustBe OK
      val result = res.json.as[AgentClientListResponse]
      result.clients must not be empty
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorisedAgent()
      val res1 = getClientList("CRED-ABC-123", "MGD")
      val result1 = res1.json.as[AgentClientListResponse]

      val res2 = getClientList("CRED-ABC-123", "MGD")
      val result2 = res2.json.as[AgentClientListResponse]

      result1.clients.length mustBe result2.clients.length
      result1.totalCount mustBe result2.totalCount
      result1.clientNameStartingCharacters mustBe result2.clientNameStartingCharacters
    }

    "return all required fields in response" in {
      AuthStub.authorisedAgent()
      val res = getClientList("CRED-ABC-123", "MGD")

      res.status mustBe OK
      val result = res.json.as[AgentClientListResponse]

      result.clients.foreach { client =>
        client.regNumber must not be empty
        client.clientName must not be empty
        client.agentOwnRef must not be empty
      }
    }

    "return distinct client name starting characters" in {
      AuthStub.authorisedAgent()
      val res = getClientList("CRED-ABC-123", "MGD")

      res.status mustBe OK
      val result = res.json.as[AgentClientListResponse]

      result.clientNameStartingCharacters.distinct mustBe result.clientNameStartingCharacters
    }
  }

  private val hasClientEndpoint = "/gambling/agent/has-client"

  private def hasClient(
                         regime: String,
                         regNumber: String,
                         credentialId: String): WSResponse = {
    val url = s"$hasClientEndpoint/$regime/$regNumber?credentialId=$credentialId"
    get(url).futureValue
  }

  "GET /has-client (stubbed repo, no DB)" should {

    "return 200 with boolean when authorised and parameters are valid" in {
      AuthStub.authorisedAgent()
      val res = hasClient("MGD", "XEM00000000640", "CRED-ABC-123")
      res.status mustBe OK
      (res.json \ "hasClient").as[Boolean] mustBe true
    }

    "return 400 when regime is empty" in {
      AuthStub.authorisedAgent()
      val res = hasClient(" ", "XEM00000000640", "CRED-ABC-123")

      res.status mustBe BAD_REQUEST
      (res.json \ "message").as[String] mustBe "Invalid Regime Code"
    }

    "return 400 when regNumber is empty" in {
      AuthStub.authorisedAgent()
      val res = hasClient("MGD", "", "CRED-ABC-123")

      res.status mustBe NOT_FOUND
      (res.json \ "message").as[String] mustBe "URI not found"
    }

    "return 400 when credentialId is empty" in {
      AuthStub.authorisedAgent()
      val res = hasClient("MGD","XEM00000000640", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId must be provided"
    }

    "return 400 when both regNumber and credentialId are empty" in {
      AuthStub.authorisedAgent()
      val res = hasClient("MGD", "", "")

      res.status mustBe NOT_FOUND
      (res.json \ "message").as[String] mustBe "URI not found"
    }

    "return 400 when regime contains only whitespace" in {
      AuthStub.authorisedAgent()
      val res = hasClient("%20%20%20", "XEM00000000640", "CRED-ABC-123")

      res.status mustBe BAD_REQUEST
      (res.json \ "message").as[String] mustBe "Invalid Regime Code"
    }

    "return 400 when regNumber contains only whitespace" in {
      AuthStub.authorisedAgent()
      val res = hasClient("MGD", "%20%20%20", "CRED-ABC-123")

      res.status mustBe BAD_REQUEST
      (res.json \ "message").as[String] mustBe "regNumber has invalid format"
    }

    "return 400 when credentialId contains only whitespace" in {
      AuthStub.authorisedAgent()
      val res = hasClient("MGD", "XEM00000000640", "%20%20%20")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId must be provided"
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = hasClient("MGD", "XEM00000000640", "CRED-ABC-123")

      res.status mustBe UNAUTHORIZED
    }


    "return 400 when credentialId parameter is missing" in {
      AuthStub.authorisedAgent()
      val res = get(s"$hasClientEndpoint/MGD/XEM00000000640").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when all parameters are missing" in {
      AuthStub.authorisedAgent()
      val res = get(hasClientEndpoint).futureValue

      res.status mustBe NOT_FOUND
    }

    "handle special characters in credentialId" in {
      AuthStub.authorisedAgent()
      val res = get(s"$hasClientEndpoint/MGD/XEM00000000640?credentialId=CRED-ABC-123%2Bspecial").futureValue

      res.status mustBe OK
      (res.json \ "hasClient").as[Boolean] mustBe true
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorisedAgent()
      val res1 = hasClient("MGD", "XEM00000000640", "CRED-ABC-123")
      (res1.json \ "hasClient").as[Boolean] mustBe true

      val res2 = hasClient("MGD", "XEM00000000640", "CRED-ABC-123")
      (res2.json \ "hasClient").as[Boolean] mustBe true
    }
  }
}
