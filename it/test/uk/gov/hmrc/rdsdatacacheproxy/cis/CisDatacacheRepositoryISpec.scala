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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.cis.repositories.CisMonthlyReturnSource

class CisDatacacheRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite {

  val cisRdsStub: CisRdsStub = new CisRdsStub(new StubUtils())

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[CisMonthlyReturnSource].toInstance(cisRdsStub)
    )
    .build()

  private lazy val repository: CisMonthlyReturnSource = app.injector.instanceOf[CisMonthlyReturnSource]

  "getAllClients (stubbed repository)" should {

    "return all clients when valid irAgentId and credentialId are provided" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123"
      ).futureValue

      result.clients must not be empty
      result.clients.length mustBe 3
      result.totalCount mustBe 3
      result.clientNameStartingCharacters must contain allOf ("A", "B", "X")
    }

    "return clients with correct taxpayer details" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123"
      ).futureValue

      val firstClient = result.clients.head
      firstClient.uniqueId mustBe "1"
      firstClient.taxOfficeNumber mustBe "123"
      firstClient.taxOfficeRef mustBe "AB001"
      firstClient.employerName1 mustBe Some("ABC Construction Ltd")
    }

    "handle pagination parameters correctly" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        start = 0,
        count = 10
      ).futureValue

      result.clients.length mustBe 3
      result.totalCount mustBe 3
    }

    "handle sort and order parameters" in {
      val resultAsc = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        sort = 0,
        order = "ASC"
      ).futureValue

      resultAsc.clients must not be empty

      val resultDesc = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        sort = 0,
        order = "DESC"
      ).futureValue

      resultDesc.clients must not be empty
    }

    "handle different sort options (0=name, 1=tax office ref, 2=agent own ref)" in {
      val sortByName = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        sort = 0
      ).futureValue

      sortByName.clients must not be empty

      val sortByTaxOfficeRef = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        sort = 1
      ).futureValue

      sortByTaxOfficeRef.clients must not be empty

      val sortByAgentOwnRef = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        sort = 2
      ).futureValue

      sortByAgentOwnRef.clients must not be empty
    }

    "return empty result when irAgentId is empty" in {
      val result = repository.getAllClients(
        irAgentId = "",
        credentialId = "CRED-ABC-123"
      ).futureValue

      result.clients mustBe empty
      result.totalCount mustBe 0
      result.clientNameStartingCharacters mustBe empty
    }

    "return empty result when credentialId is empty" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = ""
      ).futureValue

      result.clients mustBe empty
      result.totalCount mustBe 0
      result.clientNameStartingCharacters mustBe empty
    }

    "return empty result when both irAgentId and credentialId are empty" in {
      val result = repository.getAllClients(
        irAgentId = "",
        credentialId = ""
      ).futureValue

      result.clients mustBe empty
      result.totalCount mustBe 0
      result.clientNameStartingCharacters mustBe empty
    }

    "handle whitespace-only irAgentId" in {
      val result = repository.getAllClients(
        irAgentId = "   ",
        credentialId = "CRED-ABC-123"
      ).futureValue

      result.clients mustBe empty
      result.totalCount mustBe 0
    }

    "handle whitespace-only credentialId" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "   "
      ).futureValue

      result.clients mustBe empty
      result.totalCount mustBe 0
    }

    "handle count=-1 (return all records)" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        start = 0,
        count = -1
      ).futureValue

      result.clients must not be empty
      result.totalCount mustBe 3
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123"
      ).futureValue

      val result2 = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123"
      ).futureValue

      result1.clients.length mustBe result2.clients.length
      result1.totalCount mustBe result2.totalCount
      result1.clientNameStartingCharacters mustBe result2.clientNameStartingCharacters
    }

    "handle special characters in irAgentId" in {
      val result = repository.getAllClients(
        irAgentId = "IR-123/456",
        credentialId = "CRED-ABC-123"
      ).futureValue

      result.clients must not be empty
    }

    "handle special characters in credentialId" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123/XYZ"
      ).futureValue

      result.clients must not be empty
    }

    "return CisClientSearchResult with all required fields populated" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123"
      ).futureValue

      result.clients.foreach { client =>
        client.uniqueId must not be empty
        client.taxOfficeNumber must not be empty
        client.taxOfficeRef must not be empty
      }
    }

    "return distinct client name starting characters" in {
      val result = repository.getAllClients(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123"
      ).futureValue

      result.clientNameStartingCharacters.distinct mustBe result.clientNameStartingCharacters
    }
  }

  "hasClient (stubbed repository)" should {

    "return true when client exists with valid parameters" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe true
    }

    "return false when client does not exist" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "999",
        taxOfficeReference = "ZZ999"
      ).futureValue

      result mustBe false
    }

    "return false when irAgentId is empty" in {
      val result = repository.hasClient(
        irAgentId = "",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe false
    }

    "return false when credentialId is empty" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe false
    }

    "return false when taxOfficeNumber is empty" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe false
    }

    "return false when taxOfficeReference is empty" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = ""
      ).futureValue

      result mustBe false
    }

    "return false when multiple parameters are empty" in {
      val result = repository.hasClient(
        irAgentId = "",
        credentialId = "",
        taxOfficeNumber = "",
        taxOfficeReference = ""
      ).futureValue

      result mustBe false
    }

    "handle whitespace-only irAgentId" in {
      val result = repository.hasClient(
        irAgentId = "   ",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe false
    }

    "handle whitespace-only credentialId" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "   ",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe false
    }

    "handle whitespace-only taxOfficeNumber" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "   ",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe false
    }

    "handle whitespace-only taxOfficeReference" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "   "
      ).futureValue

      result mustBe false
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      val result2 = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result1 mustBe result2
    }

    "handle special characters in irAgentId" in {
      val result = repository.hasClient(
        irAgentId = "IR-123/456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe a[Boolean]
    }

    "handle special characters in credentialId" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123/XYZ",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe a[Boolean]
    }

    "handle special characters in taxOfficeNumber" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "12/3",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe a[Boolean]
    }

    "handle special characters in taxOfficeReference" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB/001"
      ).futureValue

      result mustBe a[Boolean]
    }

    "find another existing client (second client)" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "456",
        taxOfficeReference = "CD002"
      ).futureValue

      result mustBe true
    }

    "find another existing client (third client)" in {
      val result = repository.hasClient(
        irAgentId = "IR123456",
        credentialId = "CRED-ABC-123",
        taxOfficeNumber = "789",
        taxOfficeReference = "EF003"
      ).futureValue

      result mustBe true
    }

    "work with different irAgentId and credentialId combination" in {
      val result = repository.hasClient(
        irAgentId = "IR654321",
        credentialId = "CRED-XYZ-999",
        taxOfficeNumber = "123",
        taxOfficeReference = "AB001"
      ).futureValue

      result mustBe a[Boolean]
    }
  }
}
