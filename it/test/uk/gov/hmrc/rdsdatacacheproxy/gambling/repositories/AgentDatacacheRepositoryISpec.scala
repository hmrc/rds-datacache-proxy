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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime.{GBD, MGD}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.AgentDataSourceStub

import scala.language.postfixOps

class AgentDatacacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  val agentDataSourceStub: AgentDataSourceStub = new AgentDataSourceStub()

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AgentDataSource].toInstance(agentDataSourceStub)
    )
    .build()

  private lazy val repository: AgentDataSource = app.injector.instanceOf[AgentDataSource]

  "getAllClients (stubbed repository)" should {

    "return all clients when valid regime and credentialId are provided" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123"
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clients must not be empty
          aclr.clients.length mustBe 3
          aclr.totalCount mustBe 3
          aclr.clientNameStartingCharacters must contain allOf ("J", "S", "X")
          true
        case _ => false
      } mustBe true
    }

    "return clients with correct  details" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123"
        )
        .futureValue

      result match {
        case Right(aclr) =>
          val firstClient = aclr.clients.head
          firstClient.regNumber mustBe "111222333"
          firstClient.clientName mustBe "Jones Motors Ltd"
          firstClient.agentOwnRef mustBe "123"
          true
        case _ => false
      } mustBe true
    }

    "handle pagination parameters correctly" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          start        = 0,
          count        = 10
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clients must not be empty
          aclr.clients.length mustBe 3
          aclr.totalCount mustBe 3
          true
        case _ => false
      } mustBe
        true
    }

    "handle sort and order parameters" in {
      val resultAsc = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          sort         = 0,
          order        = "ASC"
        )
        .futureValue

      resultAsc match {
        case Right(aclr) =>
          aclr.clients must not be empty
          true
        case _ => false
      } mustBe
        true

      val resultDesc = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          sort         = 0,
          order        = "DESC"
        )
        .futureValue

      resultDesc match {
        case Right(aclr) =>
          aclr.clients must not be empty
          true
        case _ => false
      } mustBe
        true
    }

    "handle different sort options (0=name, 1=tax office ref, 2=agent own ref)" in {
      val sortByName = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          sort         = 0
        )
        .futureValue

      sortByName match {
        case Right(aclr) =>
          aclr.clients must not be empty
          true
        case _ => false
      } mustBe
        true

      val sortBy1 = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          sort         = 1
        )
        .futureValue

      sortBy1 match {
        case Right(aclr) =>
          aclr.clients must not be empty
          true
        case _ => false
      } mustBe
        true

      val sortBy2 = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          sort         = 2
        )
        .futureValue

      sortBy2 match {
        case Right(aclr) =>
          aclr.clients must not be empty
          true
        case _ => false
      } mustBe
        true
    }

    "return empty result when credentialId is empty" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = ""
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clients mustBe empty
          aclr.totalCount mustBe 0
          aclr.clientNameStartingCharacters mustBe empty
          true
        case _ => false
      } mustBe
        true
    }

    "handle whitespace-only credentialId" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "   "
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clients mustBe empty
          aclr.totalCount mustBe 0
          aclr.clientNameStartingCharacters mustBe empty
          true
        case _ => false
      } mustBe
        true
    }

    "handle count=-1 (return all records)" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          start        = 0,
          count        = -1
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clients must not be empty
          aclr.totalCount mustBe 3
          true
        case _ => false
      } mustBe
        true
    }

    "return consistent results across multiple calls" in {
      val result1 = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123"
        )
        .futureValue

      val result2 = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123"
        )
        .futureValue

      (result1, result2) match {
        case (Right(aclr1), Right(aclr2)) =>
          aclr1.clients.length mustBe aclr2.clients.length
          aclr1.totalCount mustBe aclr2.totalCount
          aclr1.clientNameStartingCharacters mustBe aclr2.clientNameStartingCharacters
          true
        case _ => false
      } mustBe
        true
    }

    "handle special characters in credentialId" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123/XYZ"
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clients must not be empty
          true
        case _ => false
      } mustBe
        true
    }

    "return AgentClientListResponse with all required fields populated" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123"
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clients.foreach { client =>
            client.regNumber   must not be empty
            client.clientName  must not be empty
            client.agentOwnRef must not be empty
          }
          true
        case _ => false
      } mustBe
        true
    }

    "return distinct client name starting characters" in {
      val result = repository
        .getAllClients(
          regime       = MGD,
          credentialId = "CRED-ABC-123"
        )
        .futureValue

      result match {
        case Right(aclr) =>
          aclr.clientNameStartingCharacters.distinct mustBe aclr.clientNameStartingCharacters
          true
        case _ => false
      } mustBe true
    }
  }

  "hasClient (stubbed repository)" should {

    "return true when client exists with valid parameters" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "XEM00000000640"
        )
        .futureValue

      result mustBe Right(true)
    }

    "return false when client does not exist" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "999"
        )
        .futureValue

      result mustBe Right(false)
    }

    "return false when credentialId is empty" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "",
          regNumber    = "123"
        )
        .futureValue

      result mustBe Right(false)
    }

    "return false when regNumber is empty" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = ""
        )
        .futureValue

      result mustBe Right(false)
    }

    "return false when multiple parameters are empty" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "",
          regNumber    = ""
        )
        .futureValue

      result mustBe Right(false)
    }

    "handle whitespace-only credentialId" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "   ",
          regNumber    = "123"
        )
        .futureValue

      result mustBe Right(false)
    }

    "handle whitespace-only regNumber" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "   "
        )
        .futureValue

      result mustBe Right(false)
    }

    "return consistent results across multiple calls" in {
      val result1 = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "123"
        )
        .futureValue

      val result2 = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "123"
        )
        .futureValue

      result1 mustBe result2
    }

    "handle special characters in credentialId" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123/XYZ",
          regNumber    = "XEM00000000640"
        )
        .futureValue

      result mustBe Right(true)
    }

    "handle special characters in regNumber" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "12/3"
        )
        .futureValue

      result mustBe Right(false)
    }

    "find another existing client (second client)" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "XVM00000000495"
        )
        .futureValue

      result mustBe Right(true)
    }

    "find another existing client (third client)" in {
      val result = repository
        .hasClient(
          regime       = MGD,
          credentialId = "CRED-ABC-123",
          regNumber    = "XHM00000000785"
        )
        .futureValue

      result mustBe Right(true)
    }

    "work with different regime and credentialId combination" in {
      val result = repository
        .hasClient(
          regime       = GBD,
          credentialId = "CRED-XYZ-999",
          regNumber    = "XVM00000000495"
        )
        .futureValue

      result mustBe Right(true)
    }
  }
}
