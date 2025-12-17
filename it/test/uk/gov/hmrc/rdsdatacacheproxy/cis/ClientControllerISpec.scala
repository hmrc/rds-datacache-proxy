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

package uk.gov.hmrc.rdsdatacacheproxy.cis

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.CisClientSearchResult
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class ClientControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  private val endpoint = "/cis/client-list-status"

  private def getClientListStatus(credentialId: String, serviceName: String, gracePeriod: Int = 14400): WSResponse =
    get(s"$endpoint?credentialId=$credentialId&serviceName=$serviceName&gracePeriod=$gracePeriod").futureValue

  "GET /client-list-status (stubbed repo, no DB)" should {

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

  private val clientListEndpoint = "/cis/client-list"

  private def getClientList(
    irAgentId: String,
    credentialId: String,
    start: Int = 0,
    count: Int = -1,
    sort: Int = 0,
    ascending: Boolean = true
  ): WSResponse = {
    val url = s"$clientListEndpoint?irAgentId=$irAgentId&credentialId=$credentialId&start=$start&count=$count&sort=$sort&ascending=$ascending"
    get(url).futureValue
  }

  "GET /client-list (stubbed repo, no DB)" should {

    "return 200 with client list when authorised and parameters are valid" in {
      AuthStub.authorised()
      val res = getClientList("IR123456", "CRED-ABC-123")

      res.status mustBe OK
      val result = res.json.as[CisClientSearchResult]
      result.clients must not be empty
      result.clients.length mustBe 3
      result.totalCount mustBe 3
      result.clientNameStartingCharacters must contain allOf ("A", "B", "X")
    }

    "return 200 with correct client structure" in {
      AuthStub.authorised()
      val res = getClientList("IR123456", "CRED-ABC-123")

      res.status mustBe OK
      val result = res.json.as[CisClientSearchResult]

      val firstClient = result.clients.head
      firstClient.uniqueId mustBe "1"
      firstClient.taxOfficeNumber mustBe "123"
      firstClient.taxOfficeRef mustBe "AB001"
      firstClient.employerName1 mustBe Some("ABC Construction Ltd")
    }

    "return 200 when using default parameters" in {
      AuthStub.authorised()
      val res = get(s"$clientListEndpoint?irAgentId=IR123456&credentialId=CRED-ABC-123").futureValue

      res.status mustBe OK
      val result = res.json.as[CisClientSearchResult]
      result.clients must not be empty
    }

    "handle pagination parameters correctly" in {
      AuthStub.authorised()
      val res = getClientList("IR123456", "CRED-ABC-123", start = 0, count = 10)

      res.status mustBe OK
      val result = res.json.as[CisClientSearchResult]
      result.clients.length mustBe 3
      result.totalCount mustBe 3
    }

    "handle sort parameter correctly" in {
      AuthStub.authorised()
      val res1 = getClientList("IR123456", "CRED-ABC-123", sort = 0)
      res1.status mustBe OK

      val res2 = getClientList("IR123456", "CRED-ABC-123", sort = 1)
      res2.status mustBe OK

      val res3 = getClientList("IR123456", "CRED-ABC-123", sort = 2)
      res3.status mustBe OK
    }

    "handle ascending parameter correctly" in {
      AuthStub.authorised()
      val resAsc = getClientList("IR123456", "CRED-ABC-123", ascending = true)
      resAsc.status mustBe OK

      val resDesc = getClientList("IR123456", "CRED-ABC-123", ascending = false)
      resDesc.status mustBe OK
    }

    "return 400 when irAgentId is empty" in {
      AuthStub.authorised()
      val res = getClientList("", "CRED-ABC-123")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
    }

    "return 400 when credentialId is empty" in {
      AuthStub.authorised()
      val res = getClientList("IR123456", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
    }

    "return 400 when both irAgentId and credentialId are empty" in {
      AuthStub.authorised()
      val res = getClientList("", "")

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
    }

    "return 400 when irAgentId contains only whitespace" in {
      AuthStub.authorised()
      val res = get(s"$clientListEndpoint?irAgentId=%20%20%20&credentialId=CRED-ABC-123").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
    }

    "return 400 when credentialId contains only whitespace" in {
      AuthStub.authorised()
      val res = get(s"$clientListEndpoint?irAgentId=IR123456&credentialId=%20%20%20").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = getClientList("IR123456", "CRED-ABC-123")

      res.status mustBe UNAUTHORIZED
    }

    "return 400 when irAgentId parameter is missing" in {
      AuthStub.authorised()
      val res = get(s"$clientListEndpoint?credentialId=CRED-ABC-123").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when credentialId parameter is missing" in {
      AuthStub.authorised()
      val res = get(s"$clientListEndpoint?irAgentId=IR123456").futureValue

      res.status mustBe BAD_REQUEST
    }

    "return 400 when all parameters are missing" in {
      AuthStub.authorised()
      val res = get(clientListEndpoint).futureValue

      res.status mustBe BAD_REQUEST
    }

    "handle special characters in irAgentId and credentialId" in {
      AuthStub.authorised()
      val res = get(s"$clientListEndpoint?irAgentId=IR-123%2F456&credentialId=CRED-ABC-123%2Bspecial").futureValue

      res.status mustBe OK
      val result = res.json.as[CisClientSearchResult]
      result.clients must not be empty
    }

    "return consistent results across multiple calls" in {
      AuthStub.authorised()
      val res1 = getClientList("IR123456", "CRED-ABC-123")
      val result1 = res1.json.as[CisClientSearchResult]

      val res2 = getClientList("IR123456", "CRED-ABC-123")
      val result2 = res2.json.as[CisClientSearchResult]

      result1.clients.length mustBe result2.clients.length
      result1.totalCount mustBe result2.totalCount
      result1.clientNameStartingCharacters mustBe result2.clientNameStartingCharacters
    }

    "return all required fields in response" in {
      AuthStub.authorised()
      val res = getClientList("IR123456", "CRED-ABC-123")

      res.status mustBe OK
      val result = res.json.as[CisClientSearchResult]

      result.clients.foreach { client =>
        client.uniqueId must not be empty
        client.taxOfficeNumber must not be empty
        client.taxOfficeRef must not be empty
      }
    }

    "return distinct client name starting characters" in {
      AuthStub.authorised()
      val res = getClientList("IR123456", "CRED-ABC-123")

      res.status mustBe OK
      val result = res.json.as[CisClientSearchResult]

      result.clientNameStartingCharacters.distinct mustBe result.clientNameStartingCharacters
    }
  }
}
