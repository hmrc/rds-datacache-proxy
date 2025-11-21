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

package uk.gov.hmrc.rdsdatacacheproxy.cis.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{CisClientSearchResult, CisTaxpayerSearchResult, ClientListDownloadStatus}
import uk.gov.hmrc.rdsdatacacheproxy.cis.repositories.CisDatacacheRepository

import scala.concurrent.Future

final class ClientServiceSpec extends SpecBase {

  private val repository = mock[CisDatacacheRepository]
  private val service = new ClientService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val credentialId = "cred-123"
  private val serviceName = "service-xyz"
  private val gracePeriod = 14400

  "ClientService#getClientListDownloadStatus" - {

    "return Right(InitiateDownload) when repository returns -1" in {
      when(repository.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(-1))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.InitiateDownload)
      verify(repository).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Right(InProgress) when repository returns 0" in {
      when(repository.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(0))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.InProgress)
      verify(repository).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Right(Succeeded) when repository returns 1" in {
      when(repository.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(1))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Succeeded)
      verify(repository).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Right(Failed) when repository returns 2" in {
      when(repository.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(2))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Failed)
      verify(repository).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Left with error message when repository returns unrecognized status code" in {
      when(repository.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(99))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Left("Could not map client list download status")
      verify(repository).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "use default grace period when not specified" in {
      when(repository.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(14400)))
        .thenReturn(Future.successful(1))

      val result = service.getClientListDownloadStatus(credentialId, serviceName)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Succeeded)
      verify(repository).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(14400))
      verifyNoMoreInteractions(repository)
    }

    "propagate exceptions from repository" in {
      val exception = new RuntimeException("Database error")
      when(repository.getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.failed(exception))

      val result = service.getClientListDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).failed.futureValue

      result mustBe exception
      verify(repository).getClientListDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }
  }

  "ClientService#getClientList" - {

    val irAgentId = "IR123456"
    val credentialId = "CRED-ABC-123"

    "return client search result when repository returns data with ascending=true" in {
      val expectedResult = CisClientSearchResult(
        clients = List(
          CisTaxpayerSearchResult(
            uniqueId          = "1",
            taxOfficeNumber   = "123",
            taxOfficeRef      = "AB001",
            aoDistrict        = Some("456"),
            aoPayType         = Some("M"),
            aoCheckCode       = Some("XY"),
            aoReference       = Some("REF001"),
            validBusinessAddr = Some("Y"),
            correlation       = Some("CORR001"),
            ggAgentId         = Some("GG001"),
            employerName1     = Some("ABC Ltd"),
            employerName2     = None,
            agentOwnRef       = Some("AGENT001"),
            schemeName        = Some("ABC")
          )
        ),
        totalCount                   = 1,
        clientNameStartingCharacters = List("A")
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 0, ascending = true).futureValue

      result mustBe expectedResult
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "return client search result when repository returns data with ascending=false" in {
      val expectedResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 0, ascending = false).futureValue

      result mustBe expectedResult
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC"))
      verifyNoMoreInteractions(repository)
    }

    "pass through pagination parameters correctly" in {
      val expectedResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 100,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(10), eqTo(20), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 10, 20, 0, ascending = true).futureValue

      result mustBe expectedResult
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(10), eqTo(20), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "pass through sort parameter correctly" in {
      val expectedResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(2), eqTo("ASC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 2, ascending = true).futureValue

      result mustBe expectedResult
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(2), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "convert ascending=true to ASC order" in {
      val expectedResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 0, ascending = true).futureValue

      result mustBe expectedResult
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "convert ascending=false to DESC order" in {
      val expectedResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 0, ascending = false).futureValue

      result mustBe expectedResult
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC"))
      verifyNoMoreInteractions(repository)
    }

    "return empty result when repository returns empty list" in {
      val expectedResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 0, ascending = true).futureValue

      result.clients mustBe empty
      result.totalCount mustBe 0
      result.clientNameStartingCharacters mustBe empty
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "return multiple clients when repository returns multiple results" in {
      val expectedResult = CisClientSearchResult(
        clients = List(
          CisTaxpayerSearchResult(
            uniqueId          = "1",
            taxOfficeNumber   = "123",
            taxOfficeRef      = "AB001",
            aoDistrict        = None,
            aoPayType         = None,
            aoCheckCode       = None,
            aoReference       = None,
            validBusinessAddr = None,
            correlation       = None,
            ggAgentId         = None,
            employerName1     = Some("ABC Ltd"),
            employerName2     = None,
            agentOwnRef       = None,
            schemeName        = Some("ABC")
          ),
          CisTaxpayerSearchResult(
            uniqueId          = "2",
            taxOfficeNumber   = "456",
            taxOfficeRef      = "CD002",
            aoDistrict        = None,
            aoPayType         = None,
            aoCheckCode       = None,
            aoReference       = None,
            validBusinessAddr = None,
            correlation       = None,
            ggAgentId         = None,
            employerName1     = Some("XYZ Builders"),
            employerName2     = None,
            agentOwnRef       = None,
            schemeName        = Some("XYZ")
          )
        ),
        totalCount                   = 2,
        clientNameStartingCharacters = List("A", "X")
      )

      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(expectedResult))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 0, ascending = true).futureValue

      result.clients.length mustBe 2
      result.totalCount mustBe 2
      result.clientNameStartingCharacters mustBe List("A", "X")
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "propagate exceptions from repository" in {
      val exception = new RuntimeException("Database error")
      when(repository.getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.failed(exception))

      val result = service.getClientList(irAgentId, credentialId, 0, -1, 0, ascending = true).failed.futureValue

      result mustBe exception
      verify(repository).getAllClients(eqTo(irAgentId), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "handle different irAgentId and credentialId values" in {
      val expectedResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        repository.getAllClients(
          eqTo("IR-DIFFERENT"),
          eqTo("CRED-DIFFERENT"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo("ASC")
        )
      ).thenReturn(Future.successful(expectedResult))

      val result =
        service.getClientList("IR-DIFFERENT", "CRED-DIFFERENT", 0, -1, 0, ascending = true).futureValue

      result mustBe expectedResult
      verify(repository).getAllClients(
        eqTo("IR-DIFFERENT"),
        eqTo("CRED-DIFFERENT"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo("ASC")
      )
      verifyNoMoreInteractions(repository)
    }
  }
}
