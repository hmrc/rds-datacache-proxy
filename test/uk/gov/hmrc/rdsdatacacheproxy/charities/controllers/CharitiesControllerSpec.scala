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

package uk.gov.hmrc.rdsdatacacheproxy.charities.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.charities.models.{AgentNameResponse, OrganisationNameResponse}
import uk.gov.hmrc.rdsdatacacheproxy.charities.repositories.CharitiesDataSource

import scala.concurrent.Future

class CharitiesControllerSpec extends SpecBase with MockitoSugar {

  "CharitiesController#getAgentName" - {

    "return 200 and a successful response when repository returns Some(agent name)" in new SetUp {
      when(mockCharitiesDataSource.getAgentName(any[String]))
        .thenReturn(Future.successful(Some("Test Agent Name")))

      val result: Future[Result] = controller.getAgentName("AGENT123")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(AgentNameResponse("Test Agent Name"))
      verify(mockCharitiesDataSource).getAgentName("AGENT123")
    }

    "return 404 when repository returns None" in new SetUp {
      when(mockCharitiesDataSource.getAgentName(any[String]))
        .thenReturn(Future.successful(None))

      val result: Future[Result] = controller.getAgentName("AGENT123")(fakeRequest)

      status(result) shouldBe NOT_FOUND
      verify(mockCharitiesDataSource).getAgentName("AGENT123")
    }

    "return 400 when agentRef is empty" in new SetUp {
      val result: Future[Result] = controller.getAgentName("")(fakeRequest)

      status(result)                               shouldBe BAD_REQUEST
      contentType(result)                          shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] shouldBe "agentRef must be provided"
      verifyNoInteractions(mockCharitiesDataSource)
    }

    "return 400 when agentRef is whitespace only" in new SetUp {
      val result: Future[Result] = controller.getAgentName("   ")(fakeRequest)

      status(result)                               shouldBe BAD_REQUEST
      contentType(result)                          shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] shouldBe "agentRef must be provided"
      verifyNoInteractions(mockCharitiesDataSource)
    }

    "return 500 and log error when repository call fails" in new SetUp {
      val exception = new RuntimeException("Database error")
      when(mockCharitiesDataSource.getAgentName(any[String]))
        .thenReturn(Future.failed(exception))

      val result: Future[Result] = controller.getAgentName("AGENT123")(fakeRequest)

      status(result)                               shouldBe INTERNAL_SERVER_ERROR
      contentType(result)                          shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] shouldBe "Failed to retrieve agent name"
    }
  }

  "CharitiesController#getOrganisationName" - {

    "return 200 and a successful response when repository returns Some(organisation name)" in new SetUp {
      when(mockCharitiesDataSource.getOrganisationName(any[String]))
        .thenReturn(Future.successful(Some("Test Organisation Name")))

      val result: Future[Result] = controller.getOrganisationName("CHARITY123")(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(OrganisationNameResponse("Test Organisation Name"))
      verify(mockCharitiesDataSource).getOrganisationName("CHARITY123")
    }

    "return 404 when repository returns None" in new SetUp {
      when(mockCharitiesDataSource.getOrganisationName(any[String]))
        .thenReturn(Future.successful(None))

      val result: Future[Result] = controller.getOrganisationName("CHARITY123")(fakeRequest)

      status(result) shouldBe NOT_FOUND
      verify(mockCharitiesDataSource).getOrganisationName("CHARITY123")
    }

    "return 400 when charityRef is empty" in new SetUp {
      val result: Future[Result] = controller.getOrganisationName("")(fakeRequest)

      status(result)                               shouldBe BAD_REQUEST
      contentType(result)                          shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] shouldBe "charityRef must be provided"
      verifyNoInteractions(mockCharitiesDataSource)
    }

    "return 400 when charityRef is whitespace only" in new SetUp {
      val result: Future[Result] = controller.getOrganisationName("   ")(fakeRequest)

      status(result)                               shouldBe BAD_REQUEST
      contentType(result)                          shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] shouldBe "charityRef must be provided"
      verifyNoInteractions(mockCharitiesDataSource)
    }

    "return 500 and log error when repository call fails" in new SetUp {
      val exception = new RuntimeException("Database error")
      when(mockCharitiesDataSource.getOrganisationName(any[String]))
        .thenReturn(Future.failed(exception))

      val result: Future[Result] = controller.getOrganisationName("CHARITY123")(fakeRequest)

      status(result)                               shouldBe INTERNAL_SERVER_ERROR
      contentType(result)                          shouldBe Some("application/json")
      (contentAsJson(result) \ "error").as[String] shouldBe "Failed to retrieve organisation name"
    }
  }

  private class SetUp {
    val mockCharitiesDataSource: CharitiesDataSource = mock[CharitiesDataSource]
    val controller: CharitiesController = new CharitiesController(fakeAuthAction, mockCharitiesDataSource, cc)
  }
}
