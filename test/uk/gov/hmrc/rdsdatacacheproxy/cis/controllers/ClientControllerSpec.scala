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

package uk.gov.hmrc.rdsdatacacheproxy.cis.controllers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{CisClientSearchResult, CisTaxpayerSearchResult, ClientListDownloadStatus}
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.ClientService

import scala.concurrent.{ExecutionContext, Future}

class ClientControllerSpec extends SpecBase with MockitoSugar {
  "ClientController#getClientListDownloadStatus" - {

    "returns 200 with status 'InitiateDownload' when service returns Right(InitiateDownload)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.InitiateDownload)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "status").as[String] mustBe "InitiateDownload"
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
      verifyNoMoreInteractions(mockService)
    }

    "returns 200 with status 'InProgress' when service returns Right(InProgress)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.InProgress)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "InProgress"
    }

    "returns 200 with status 'Succeeded' when service returns Right(Succeeded)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "Succeeded"
    }

    "returns 200 with status 'Failed' when service returns Right(Failed)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Failed)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      (contentAsJson(res) \ "status").as[String] mustBe "Failed"
    }

    "returns 500 with error message when service returns Left(error)" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Left("Could not map client list download status")))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe INTERNAL_SERVER_ERROR
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "Could not map client list download status"
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
      verifyNoMoreInteractions(mockService)
    }

    "uses default grace period when not specified" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      status(res) mustBe OK
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext])
    }

    "propagates exceptions from service" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(14400))(using any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=14400")
      val res = controller.getClientListDownloadStatus("cred-123", "service-xyz")(req)

      whenReady(res.failed) { ex =>
        ex mustBe a[RuntimeException]
        ex.getMessage mustBe "Database error"
      }
    }

    "handles custom grace period values" in new Setup {
      when(mockService.getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(7200))(using any[ExecutionContext]))
        .thenReturn(Future.successful(Right(ClientListDownloadStatus.Succeeded)))

      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=service-xyz&gracePeriod=7200")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "service-xyz", 7200)(req)

      status(res) mustBe OK
      verify(mockService).getClientListDownloadStatus(eqTo("cred-123"), eqTo("service-xyz"), eqTo(7200))(using any[ExecutionContext])
    }

    "returns 400 when credentialId is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list-status?credentialId=&serviceName=service-xyz&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("", "service-xyz")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and serviceName must be provided"
      verifyNoInteractions(mockService)
    }

    "returns 400 when serviceName is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list-status?credentialId=cred-123&serviceName=&gracePeriod=14400")
      val res: Future[Result] = controller.getClientListDownloadStatus("cred-123", "")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and serviceName must be provided"
      verifyNoInteractions(mockService)
    }
  }

  "ClientController#getClientList" - {

    "returns 200 with client list when valid parameters provided" in new Setup {
      val mockResult = CisClientSearchResult(
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

      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )
      ).thenReturn(Future.successful(mockResult))

      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.getClientList("IR123456", "CRED-ABC-123")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)
      (json \ "totalCount").as[Int] mustBe 1
      (json \ "clients").as[List[CisTaxpayerSearchResult]].length mustBe 1
      (json \ "clientNameStartingCharacters").as[List[String]] mustBe List("A")

      verify(mockService).getClientList(
        eqTo("IR123456"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo(true)
      )
      verifyNoMoreInteractions(mockService)
    }

    "returns 200 with empty client list when no clients found" in new Setup {
      val mockResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )
      ).thenReturn(Future.successful(mockResult))

      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.getClientList("IR123456", "CRED-ABC-123")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)
      (json \ "totalCount").as[Int] mustBe 0
      (json \ "clients").as[List[CisTaxpayerSearchResult]].length mustBe 0
    }

    "handles pagination parameters correctly" in new Setup {
      val mockResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 100,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(10),
          eqTo(20),
          eqTo(0),
          eqTo(true)
        )
      ).thenReturn(Future.successful(mockResult))

      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123&start=10&count=20")
      val res: Future[Result] = controller.getClientList("IR123456", "CRED-ABC-123", 10, 20)(req)

      status(res) mustBe OK
      verify(mockService).getClientList(
        eqTo("IR123456"),
        eqTo("CRED-ABC-123"),
        eqTo(10),
        eqTo(20),
        eqTo(0),
        eqTo(true)
      )
    }

    "handles sort parameter correctly" in new Setup {
      val mockResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(2),
          eqTo(true)
        )
      ).thenReturn(Future.successful(mockResult))

      val req =
        FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123&sort=2")
      val res: Future[Result] = controller.getClientList("IR123456", "CRED-ABC-123", 0, -1, 2)(req)

      status(res) mustBe OK
      verify(mockService).getClientList(
        eqTo("IR123456"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(2),
        eqTo(true)
      )
    }

    "handles ascending=false parameter correctly" in new Setup {
      val mockResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(false)
        )
      ).thenReturn(Future.successful(mockResult))

      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123&ascending=false")
      val res: Future[Result] = controller.getClientList("IR123456", "CRED-ABC-123", 0, -1, 0, false)(req)

      status(res) mustBe OK
      verify(mockService).getClientList(
        eqTo("IR123456"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo(false)
      )
    }

    "uses default parameters when not specified" in new Setup {
      val mockResult = CisClientSearchResult(
        clients                      = List.empty,
        totalCount                   = 0,
        clientNameStartingCharacters = List.empty
      )

      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )
      ).thenReturn(Future.successful(mockResult))

      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.getClientList("IR123456", "CRED-ABC-123")(req)

      status(res) mustBe OK
      verify(mockService).getClientList(
        eqTo("IR123456"),
        eqTo("CRED-ABC-123"),
        eqTo(0),
        eqTo(-1),
        eqTo(0),
        eqTo(true)
      )
    }

    "returns 400 when irAgentId is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list?irAgentId=&credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.getClientList("", "CRED-ABC-123")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
      verifyNoInteractions(mockService)
    }

    "returns 400 when credentialId is empty" in new Setup {
      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=")
      val res: Future[Result] = controller.getClientList("IR123456", "")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
      verifyNoInteractions(mockService)
    }

    "returns 400 when both irAgentId and credentialId are empty" in new Setup {
      val req = FakeRequest(GET, "/client-list?irAgentId=&credentialId=")
      val res: Future[Result] = controller.getClientList("", "")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
      verifyNoInteractions(mockService)
    }

    "returns 400 when irAgentId contains only whitespace" in new Setup {
      val req = FakeRequest(GET, "/client-list?irAgentId=%20%20%20&credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.getClientList("   ", "CRED-ABC-123")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
      verifyNoInteractions(mockService)
    }

    "returns 400 when credentialId contains only whitespace" in new Setup {
      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=%20%20%20")
      val res: Future[Result] = controller.getClientList("IR123456", "   ")(req)

      status(res) mustBe BAD_REQUEST
      contentType(res) mustBe Some(JSON)
      (contentAsJson(res) \ "error").as[String] mustBe "credentialId and irAgentId must be provided"
      verifyNoInteractions(mockService)
    }

    "propagates exceptions from service" in new Setup {
      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )
      ).thenReturn(Future.failed(new RuntimeException("Database error")))

      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123")
      val res = controller.getClientList("IR123456", "CRED-ABC-123")(req)

      whenReady(res.failed) { ex =>
        ex mustBe a[RuntimeException]
        ex.getMessage mustBe "Database error"
      }
    }

    "returns multiple clients with correct data structure" in new Setup {
      val mockResult = CisClientSearchResult(
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

      when(
        mockService.getClientList(
          eqTo("IR123456"),
          eqTo("CRED-ABC-123"),
          eqTo(0),
          eqTo(-1),
          eqTo(0),
          eqTo(true)
        )
      ).thenReturn(Future.successful(mockResult))

      val req = FakeRequest(GET, "/client-list?irAgentId=IR123456&credentialId=CRED-ABC-123")
      val res: Future[Result] = controller.getClientList("IR123456", "CRED-ABC-123")(req)

      status(res) mustBe OK
      contentType(res) mustBe Some(JSON)

      val json = contentAsJson(res)
      (json \ "totalCount").as[Int] mustBe 2
      (json \ "clients").as[List[CisTaxpayerSearchResult]].length mustBe 2
      (json \ "clientNameStartingCharacters").as[List[String]] mustBe List("A", "X")
    }
  }

  private trait Setup {
    val mockService: ClientService = mock[ClientService]
    val controller = new ClientController(fakeAuthAction, mockService, cc)(using ec)
  }
}
