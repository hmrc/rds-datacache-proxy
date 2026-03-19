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

package uk.gov.hmrc.rdsdatacacheproxy.nova.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.nova.models.{NovaClient, NovaClientListResponse}
import uk.gov.hmrc.rdsdatacacheproxy.nova.repositories.NovaDataSource

import scala.concurrent.Future

class NovaClientControllerSpec extends SpecBase {

  private val testClientList = NovaClientListResponse(
    clients = List(
      NovaClient("Jones Motors Ltd", "111222333"),
      NovaClient("Smith Supplies", "444555666")
    ),
    totalCount                   = 2,
    clientNameStartingCharacters = List("J", "S")
  )

  "NovaClientController#getClientListDownloadStatus" - {

    "return 200 with status when repository returns a value" in new SetUp {
      when(mockNovaDataSource.getClientListDownloadStatus(any[String], any[String], any[Int]))
        .thenReturn(Future.successful(1))

      val result: Future[Result] = controller.getClientListDownloadStatus("cred-123", "NOVA", 14400)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "status").as[Int] mustBe 1
      verify(mockNovaDataSource).getClientListDownloadStatus("cred-123", "NOVA", 14400)
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getClientListDownloadStatus(any[String], any[String], any[Int]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getClientListDownloadStatus("cred-123", "NOVA", 14400)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve client list download status"
    }
  }

  "NovaClientController#getAllClients" - {

    "return 200 with client list when repository returns results" in new SetUp {
      when(mockNovaDataSource.getAllClients(any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(testClientList))

      val result: Future[Result] = controller.getAllClients("cred-123", 0, -1, 0, ascending = true)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testClientList)
      verify(mockNovaDataSource).getAllClients("cred-123", 0, -1, 0, "ASC")
    }

    "pass DESC order when ascending is false" in new SetUp {
      when(mockNovaDataSource.getAllClients(any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(testClientList))

      controller.getAllClients("cred-123", 0, -1, 0, ascending = false)(fakeRequest).futureValue

      verify(mockNovaDataSource).getAllClients("cred-123", 0, -1, 0, "DESC")
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getAllClients(any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getAllClients("cred-123", 0, -1, 0, ascending = true)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve client list"
    }
  }

  "NovaClientController#searchClients" - {

    "call getClientsByVrn when vrn param is provided" in new SetUp {
      when(mockNovaDataSource.getClientsByVrn(any[String], any[String]))
        .thenReturn(Future.successful(testClientList))

      val result: Future[Result] =
        controller.searchClients("cred-123", vrn = Some("111222333"), name = None, nameStart = None, 0, -1, 0, ascending = true)(fakeRequest)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(testClientList)
      verify(mockNovaDataSource).getClientsByVrn("cred-123", "111222333")
      verifyNoMoreInteractions(mockNovaDataSource)
    }

    "call getClientsByName when name param is provided" in new SetUp {
      when(mockNovaDataSource.getClientsByName(any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(testClientList))

      val result: Future[Result] =
        controller.searchClients("cred-123", vrn = None, name = Some("Jones"), nameStart = None, 0, -1, 0, ascending = true)(fakeRequest)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(testClientList)
      verify(mockNovaDataSource).getClientsByName("cred-123", "Jones", 0, -1, 0, "ASC")
      verifyNoMoreInteractions(mockNovaDataSource)
    }

    "call getClientsByNameStart when nameStart param is provided" in new SetUp {
      when(mockNovaDataSource.getClientsByNameStart(any[String], any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(testClientList))

      val result: Future[Result] =
        controller.searchClients("cred-123", vrn = None, name = None, nameStart = Some("J"), 0, -1, 0, ascending = true)(fakeRequest)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(testClientList)
      verify(mockNovaDataSource).getClientsByNameStart("cred-123", "J", 0, -1, 0, "ASC")
      verifyNoMoreInteractions(mockNovaDataSource)
    }

    "fall back to getAllClients when no search params are provided" in new SetUp {
      when(mockNovaDataSource.getAllClients(any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(testClientList))

      val result: Future[Result] =
        controller.searchClients("cred-123", vrn = None, name = None, nameStart = None, 0, -1, 0, ascending = true)(fakeRequest)

      status(result) mustBe OK
      verify(mockNovaDataSource).getAllClients("cred-123", 0, -1, 0, "ASC")
      verifyNoMoreInteractions(mockNovaDataSource)
    }

    "prefer vrn over name when both are provided" in new SetUp {
      when(mockNovaDataSource.getClientsByVrn(any[String], any[String]))
        .thenReturn(Future.successful(testClientList))

      controller
        .searchClients("cred-123", vrn = Some("111222333"), name = Some("Jones"), nameStart = None, 0, -1, 0, ascending = true)(fakeRequest)
        .futureValue

      verify(mockNovaDataSource).getClientsByVrn("cred-123", "111222333")
      verifyNoMoreInteractions(mockNovaDataSource)
    }

    "treat whitespace-only search params as absent" in new SetUp {
      when(mockNovaDataSource.getAllClients(any[String], any[Int], any[Int], any[Int], any[String]))
        .thenReturn(Future.successful(testClientList))

      controller
        .searchClients("cred-123", vrn = Some("  "), name = Some("  "), nameStart = Some("  "), 0, -1, 0, ascending = true)(fakeRequest)
        .futureValue

      verify(mockNovaDataSource).getAllClients("cred-123", 0, -1, 0, "ASC")
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getClientsByVrn(any[String], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] =
        controller.searchClients("cred-123", vrn = Some("111222333"), name = None, nameStart = None, 0, -1, 0, ascending = true)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to search clients"
    }
  }

  "NovaClientController#hasClient" - {

    "return 200 with exists=true when client is found" in new SetUp {
      when(mockNovaDataSource.hasClient(any[String], any[String]))
        .thenReturn(Future.successful(true))

      val result: Future[Result] = controller.hasClient("cred-123", "111222333")(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "exists").as[Boolean] mustBe true
      verify(mockNovaDataSource).hasClient("cred-123", "111222333")
    }

    "return 200 with exists=false when client is not found" in new SetUp {
      when(mockNovaDataSource.hasClient(any[String], any[String]))
        .thenReturn(Future.successful(false))

      val result: Future[Result] = controller.hasClient("cred-123", "111222333")(fakeRequest)

      status(result) mustBe OK
      (contentAsJson(result) \ "exists").as[Boolean] mustBe false
    }

    "return 400 when vrn is empty" in new SetUp {
      val result: Future[Result] = controller.hasClient("cred-123", "")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] mustBe "vrn must be provided"
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 400 when vrn is whitespace only" in new SetUp {
      val result: Future[Result] = controller.hasClient("cred-123", "   ")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] mustBe "vrn must be provided"
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.hasClient(any[String], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.hasClient("cred-123", "111222333")(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to check client existence"
    }
  }

  private class SetUp {
    val mockNovaDataSource: NovaDataSource = mock[NovaDataSource]
    val controller: NovaClientController = new NovaClientController(fakeAuthAction, mockNovaDataSource, cc)
  }
}
