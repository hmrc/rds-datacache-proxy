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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.{AgentClient, AgentClientListResponse, ClientListDownloadStatus}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.InvalidRegimeCode
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.AgentService

import scala.concurrent.{ExecutionContext, Future}

class AgentControllerSpec extends SpecBase with MockitoSugar {
  "AgentController#getClientListDownloadStatus" - {

    "returns 200 with status 'InitiateDownload' when service returns Right(InitiateDownload)" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.InitiateDownload)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "status").as[String] mustBe "InitiateDownload"
      verify(mockService).getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
      verifyNoMoreInteractions(mockService)
    }

    "returns 200 with status 'InProgress' when service returns Right(InProgress)" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.InProgress)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "InProgress"
    }

    "returns 200 with status 'Succeeded' when service returns Right(Succeeded)" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "Succeeded"
    }

    "returns 200 with status 'Failed' when service returns Right(Failed)" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Failed)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "Failed"
    }

    "returns 500 with error message when service returns Left(error)" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Left("Could not map client list download status")))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "Could not map client list download status"
      verify(mockService).getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
      verifyNoMoreInteractions(mockService)
    }

    "uses default grace period when not specified" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      verify(mockService).getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
    }

    "propagates exceptions from service" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz&gracePeriod=14400")
      val res = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      whenReady(res.failed) { ex =>
        ex mustBe a[RuntimeException]
        ex.getMessage mustBe "Database error"
      }
    }

    "handles custom grace period values" in new Setup {
      when(mockService.getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(7200))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=service-xyz&gracePeriod=7200")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz", 7200)(req)

      status(res) mustBe OK
      verify(mockService).getAllClientsDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(7200))(using any[ExecutionContext])
    }

    "returns 400 when credentialId is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list-status?credentialId=&regime=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("", "service-xyz")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and regime must be provided"
      verifyNoInteractions(mockService)
    }

    "returns 400 when regime is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&regime=&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and regime must be provided"
      verifyNoInteractions(mockService)
    }
  }

  "AgentController#getAllClients" - {

    "returns 200 with client list when valid parameters provided" in new Setup {
      val mockResult = AgentClientListResponse(
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

      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(mockResult)))

      val req = FakeRequest(GET, "/client-list/CRED-ABC-123/MGD")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "MGD")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)
      (json \ "totalCount").as[Int] mustBe 1
      (json \ "clients").as[List[AgentClient]].length mustBe 1
      (json \ "clientNameStartingCharacters").as[List[String]] mustBe List("A")

      verify(mockService).getAllClients(
        eqTo("MGD"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo(true)
      )(using any[HeaderCarrier])
      verifyNoMoreInteractions(mockService)
    }

    "returns 200 with empty client list when no clients found" in new Setup {
      val mockResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(mockResult)))

      val req = FakeRequest(GET, "/client-list/CRED-ABC-123/MGD")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "MGD")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)
      (json \ "totalCount").as[Int] mustBe 0
      (json \ "clients").as[List[AgentClient]].length mustBe 0
    }

    "handles pagination parameters correctly" in new Setup {
      val mockResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 100,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(10),
          eqTo(20),
          eqTo(0),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(mockResult)))

      val req = FakeRequest(GET, "/client-list/CRED-ABC-123/MGD?start=10&count=20")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "MGD", 10, 20)(req)

      status(res) mustBe OK
      verify(mockService).getAllClients(
        eqTo("MGD"),
        eqTo("CRED-ABC-123"),
        eqTo(10),
        eqTo(20),
        eqTo(0),
        eqTo(true)
      )(using any[HeaderCarrier])
    }

    "handles sort parameter correctly" in new Setup {
      val mockResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(2),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(mockResult)))

      val req =
        FakeRequest(GET, "/client-list/CRED-ABC-123/MGD?sort=2")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "MGD", 0, -1, 2)(req)

      status(res) mustBe OK
      verify(mockService).getAllClients(
        eqTo("MGD"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(2),
        eqTo(true)
      )(using any[HeaderCarrier])
    }

    "handles ascending=false parameter correctly" in new Setup {
      val mockResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(false)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(mockResult)))

      val req = FakeRequest(GET, "/client-list/CRED-ABC-123/MGD?ascending=false")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "MGD", 0, -1, 0, false)(req)

      status(res) mustBe OK
      verify(mockService).getAllClients(
        eqTo("MGD"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo(false)
      )(using any[HeaderCarrier])
    }

    "uses default parameters when not specified" in new Setup {
      val mockResult = AgentClientListResponse(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(mockResult)))

      val req = FakeRequest(GET, "/client-list/CRED-ABC-123/MGD")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "MGD")(req)

      status(res) mustBe OK
      verify(mockService).getAllClients(
        eqTo("MGD"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo(true)
      )(using any[HeaderCarrier])
    }

    "returns 400 when regime is empty" in new Setup {
      when(
        mockService.getAllClients(
          eqTo(""),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Left(InvalidRegimeCode)))

      val req = FakeRequest(GET, "/has-client//123?credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REGIME_CODE",
        "message" -> "Invalid Regime Code"
      )

      verify(mockService).getAllClients(
        eqTo(""),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo(true)
      )(using any[HeaderCarrier])
    }

    "returns 400 when credentialId is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list/MGD/")
      val res: Future[Result] = controller.getAllClients("", "MGD")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId must be provided"
    }

    "returns 400 when credentialId contains only whitespace" in new Setup {
      val req = FakeRequest(GET, "/has-client/MGD/123?credentialId=%20%20%20")
      val res: Future[Result] = controller.getAllClients("   ", "MGD")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId must be provided"
      verifyNoInteractions(mockService)
    }

    "propagates exceptions from service" in new Setup {
      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.failed(new RuntimeException("Database error")))

      val req = FakeRequest(GET, "/client-list/CRED-ABC-123/MGD")
      val res = controller.getAllClients("CRED-ABC-123", "MGD")(req)

      whenReady(res.failed) { ex =>
        ex mustBe a[RuntimeException]
        ex.getMessage mustBe "Database error"
      }
    }

    "returns multiple clients with correct data structure" in new Setup {
      val mockResult = AgentClientListResponse(
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

      when(
        mockService.getAllClients(
          eqTo("MGD"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )(using any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(mockResult)))

      val req = FakeRequest(GET, "/client-list/CRED-ABC-123/MGD")
      val res: Future[Result] = controller.getAllClients("CRED-ABC-123", "MGD")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)
      (json \ "totalCount").as[Int] mustBe 2
      (json \ "clients").as[List[AgentClient]].length mustBe 2
      (json \ "clientNameStartingCharacters").as[List[String]] mustBe List("A", "X")
    }
  }

  "AgentController#hasClient" - {

    "returns 200 with hasClient=true when client exists" in new Setup {
      when(
        mockService.hasClient(eqTo("MGD"), eqTo("CRED-ABC-123"), eqTo("123"))
      ).thenReturn(Future.successful(Right(true)))

      val req = FakeRequest(GET, "/has-client/MGD/123?credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.hasClient("MGD", "123", "CRED-ABC-123")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "hasClient").as[Boolean] mustBe true
      verify(mockService).hasClient(eqTo("MGD"), eqTo("CRED-ABC-123"), eqTo("123"))
      verifyNoMoreInteractions(mockService)
    }

    "returns 200 with hasClient=false when client does not exist" in new Setup {
      when(
        mockService.hasClient(eqTo("MGD"), eqTo("CRED-ABC-123"), eqTo("456"))
      ).thenReturn(Future.successful(Right(false)))

      val req = FakeRequest(GET, "/has-client/MGD/456?credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.hasClient("MGD", "456", "CRED-ABC-123")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "hasClient").as[Boolean] mustBe false
      verify(mockService).hasClient(
        eqTo("MGD"),
        eqTo("CRED-ABC-123"),
        eqTo("456")
      )
      verifyNoMoreInteractions(mockService)
    }

    "returns 400 when regime is empty" in new Setup {
      when(
        mockService.hasClient(
          eqTo(""),
          eqTo("CRED-ABC-123"),
          eqTo("reg-number")
        )
      ).thenReturn(Future.successful(Left(InvalidRegimeCode)))

      val req = FakeRequest(GET, "/has-client//reg-number?credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.hasClient("", "reg-number", "CRED-ABC-123")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      contentAsJson(res) mustBe Json.obj(
        "code"    -> "INVALID_REGIME_CODE",
        "message" -> "Invalid Regime Code"
      )

      verify(mockService).hasClient(
        eqTo(""),
        eqTo("CRED-ABC-123"),
        eqTo("reg-number")
      )
    }

    "returns 400 when credentialId is empty" in new Setup {
      val req = FakeRequest(GET, "/has-client/MGD/123")
      val res: Future[Result] = controller.hasClient("MGD", "123", "")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId must be provided"
      verifyNoInteractions(mockService)
    }

    "propagates exceptions from service" in new Setup {
      when(
        mockService.hasClient(eqTo("MGD"), eqTo("CRED-ABC-123"), eqTo("123"))
      ).thenReturn(Future.failed(new RuntimeException("Database error")))

      val req = FakeRequest(GET, "/has-client/MGD/123?credentialId=CRED-ABC-123")
      val res = controller.hasClient("MGD", "123", "CRED-ABC-123")(req)

      whenReady(res.failed) { ex =>
        ex mustBe a[RuntimeException]
        ex.getMessage mustBe "Database error"
      }
    }
  }

  private trait Setup {
    val mockService: AgentService = mock[AgentService]
    val controller = new AgentController(fakeAuthAction, mockService, cc)(using ec)
  }
}
