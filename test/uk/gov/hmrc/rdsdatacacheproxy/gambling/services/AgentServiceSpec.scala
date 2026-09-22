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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.{AgentClient, AgentClientListResponse, ClientListDownloadStatus}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.AgentDataSource

import scala.concurrent.Future

final class AgentServiceSpec extends SpecBase {

  private val repository = mock[AgentDataSource]
  private val service = new AgentService(repository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(repository)
  }

  private val validRegime = Regime.MGD
  private val validRegimeString = "MGD"

  private val credentialId = "cred-123"
  private val serviceName = "service-xyz"
  private val gracePeriod = 14400
  private val invalidRegNumber = "123"
  private val validMgdRegNumber = "XWM12345678901"

  "ClientService#getAllClientsDownloadStatus" - {

    "return Right(InitiateDownload) when repository returns -1" in {
      when(repository.getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(-1))

      val result = service.getAllClientsDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.InitiateDownload)
      verify(repository).getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Right(InProgress) when repository returns 0" in {
      when(repository.getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(0))

      val result = service.getAllClientsDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.InProgress)
      verify(repository).getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Right(Succeeded) when repository returns 1" in {
      when(repository.getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(1))

      val result = service.getAllClientsDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Succeeded)
      verify(repository).getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Right(Failed) when repository returns 2" in {
      when(repository.getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(2))

      val result = service.getAllClientsDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Failed)
      verify(repository).getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "return Left with error message when repository returns unrecognized status code" in {
      when(repository.getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.successful(99))

      val result = service.getAllClientsDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).futureValue

      result mustBe Left("Could not map client list download status")
      verify(repository).getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }

    "use default grace period when not specified" in {
      when(repository.getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(14400)))
        .thenReturn(Future.successful(1))

      val result = service.getAllClientsDownloadStatus(credentialId, serviceName)(using ec).futureValue

      result mustBe Right(ClientListDownloadStatus.Succeeded)
      verify(repository).getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(14400))
      verifyNoMoreInteractions(repository)
    }

    "propagate exceptions from repository" in {
      val exception = new RuntimeException("Database error")
      when(repository.getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod)))
        .thenReturn(Future.failed(exception))

      val result = service.getAllClientsDownloadStatus(credentialId, serviceName, gracePeriod)(using ec).failed.futureValue

      result mustBe exception
      verify(repository).getAllClientsDownloadStatus(eqTo(credentialId), eqTo(serviceName), eqTo(gracePeriod))
      verifyNoMoreInteractions(repository)
    }
  }

  "ClientService#getAllClients" - {

    "return client search result when repository returns data with ascending=true" in {
      val expectedResult = AgentClientListResponse(
        clients = List(
          AgentClient(
            clientName  = "clientName",
            regNumber   = "regNumber",
            agentOwnRef = "agentOwnRef"
          )
        ),
        totalCount                   = 1,
        clientNameStartingCharacters = List("A")
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 0, ascending = true).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "return client search result when repository returns data with ascending=false" in {
      val expectedResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 0, ascending = false).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC"))
      verifyNoMoreInteractions(repository)
    }

    "pass through pagination parameters correctly" in {
      val expectedResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 100,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(10), eqTo(20), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 10, 20, 0, ascending = true).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(10), eqTo(20), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "pass through sort parameter correctly" in {
      val expectedResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(2), eqTo("ASC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 2, ascending = true).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(2), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "convert ascending=true to ASC order" in {
      val expectedResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 0, ascending = true).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "convert ascending=false to DESC order" in {
      val expectedResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 0, ascending = false).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("DESC"))
      verifyNoMoreInteractions(repository)
    }

    "return empty result when repository returns empty list" in {
      val expectedResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 0, ascending = true).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "return multiple clients when repository returns multiple results" in {
      val expectedResult = AgentClientListResponse(
        clients = List(
          AgentClient(
            clientName  = "clientName",
            regNumber   = "regNumber",
            agentOwnRef = "agentOwnRef"
          ),
          AgentClient(
            clientName  = "clientName2",
            regNumber   = "regNumber2",
            agentOwnRef = "agentOwnRef2"
          )
        ),
        totalCount                   = 2,
        clientNameStartingCharacters = List("A", "X")
      )

      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 0, ascending = true).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "propagate exceptions from repository" in {
      when(repository.getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC")))
        .thenReturn(Future.successful(Left(UnexpectedError)))

      val result = service.getAllClients(validRegimeString, credentialId, 0, -1, 0, ascending = true).futureValue
      result mustBe Left(UnexpectedError)

      verify(repository).getAllClients(eqTo(validRegime), eqTo(credentialId), eqTo(0), eqTo(-1), eqTo(0), eqTo("ASC"))
      verifyNoMoreInteractions(repository)
    }

    "handle different regime and credentialId values" in {
      val expectedResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        repository.getAllClients(
          eqTo(Regime.MGD),
          eqTo("CRED-DIFFERENT"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo("ASC")
        )
      ).thenReturn(Future.successful(Right(expectedResult)))

      val result = service.getAllClients(validRegimeString, "CRED-DIFFERENT", 0, -1, 0, ascending = true).futureValue
      result mustBe Right(expectedResult)

      verify(repository).getAllClients(
        eqTo(Regime.MGD),
        eqTo("CRED-DIFFERENT"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo("ASC")
      )

      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeError and not call repository when Regime input is invalid" in {
      val result = service.getAllClients("INVALID", credentialId, 0, 10, 1, true).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }
  }

  "ClientService#hasClient" - {

    "return true when repository returns true" in {
      when(
        repository.hasClient(
          eqTo(validRegime),
          eqTo(credentialId),
          eqTo(validMgdRegNumber)
        )
      ).thenReturn(Future.successful(Right(true)))

      val result = service.hasClient(validRegimeString, credentialId, validMgdRegNumber).futureValue

      result mustBe Right(true)
      verify(repository).hasClient(
        eqTo(validRegime),
        eqTo(credentialId),
        eqTo(validMgdRegNumber)
      )
      verifyNoMoreInteractions(repository)
    }

    "return false when repository returns false" in {
      when(
        repository.hasClient(
          eqTo(validRegime),
          eqTo(credentialId),
          eqTo(validMgdRegNumber)
        )
      ).thenReturn(Future.successful(Right(false)))

      val result = service.hasClient(validRegimeString, credentialId, validMgdRegNumber).futureValue

      result mustBe Right(false)
      verify(repository).hasClient(
        eqTo(validRegime),
        eqTo(credentialId),
        eqTo(validMgdRegNumber)
      )
      verifyNoMoreInteractions(repository)
    }

    "propagate exceptions from repository" in {
      when(
        repository.hasClient(
          eqTo(validRegime),
          eqTo(credentialId),
          eqTo(validMgdRegNumber)
        )
      ).thenReturn(Future.successful(Left(UnexpectedError)))

      val result = service.hasClient(validRegimeString, credentialId, validMgdRegNumber).futureValue

      result mustBe Left(UnexpectedError)

      verify(repository).hasClient(
        eqTo(validRegime),
        eqTo(credentialId),
        eqTo(validMgdRegNumber)
      )
      verifyNoMoreInteractions(repository)
    }

    "handle different parameter values" in {
      when(
        repository.hasClient(
          eqTo(Regime.GBD),
          eqTo("CRED-DIFFERENT"),
          eqTo("XWA00003000000")
        )
      ).thenReturn(Future.successful(Right(true)))

      val result = service.hasClient("GBD", "CRED-DIFFERENT", "XWA00003000000").futureValue

      result mustBe Right(true)
      verify(repository).hasClient(
        eqTo(Regime.GBD),
        eqTo("CRED-DIFFERENT"),
        eqTo("XWA00003000000")
      )
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegimeError and not call repository when Regime input is invalid" in {
      val result = service.hasClient("WRONG", "CRED-DIFFERENT", validMgdRegNumber).futureValue
      result mustBe Left(InvalidRegimeCode)
      verifyNoMoreInteractions(repository)
    }

    "return InvalidRegNumber and not call repository when RegNumber input is invalid" in {
      val result = service.hasClient("GBD", credentialId, invalidRegNumber).futureValue
      result mustBe Left(InvalidRegNumber)
      verifyNoMoreInteractions(repository)
    }
  }
}
