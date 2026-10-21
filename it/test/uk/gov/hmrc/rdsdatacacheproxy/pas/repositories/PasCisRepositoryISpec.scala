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

package uk.gov.hmrc.rdsdatacacheproxy.pas.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*
import uk.gov.hmrc.rdsdatacacheproxy.pas.stub.PasCisStubData

import java.time.LocalDate
import scala.concurrent.Future

class PasCisRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class PasCisCacheRepositoryStub extends PasCisSource {
    override def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse] =
      Future.successful(
        PasCisStubData.makeCreateCisAppealResponse(request)
      )

    override def getCisAppealablePenalties(request: GetCisAppealablePenaltiesRequest): Future[GetCisAppealablePenaltiesResponse] =
      Future.successful(
        PasCisStubData.makeGetCisAppealablePenaltiesResponse(request)
      )

    override def getCisAppealablePenalty(request: GetCisAppealablePenaltyRequest): Future[GetCisAppealablePenaltyResponse] =
      Future.successful(
        PasCisStubData.makeGetCisAppealablePenaltyResponse(request)
      )

    override def getCisPenaltyAllowed(request: GetCisPenaltyAllowedRequest): Future[GetCisPenaltyAllowedResponse] =
      Future.successful(
        PasCisStubData.makeGetCisPenaltyAllowedResponse(request)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PasCisSource].toInstance(new PasCisCacheRepositoryStub)
      )
      .build()

  private lazy val repository = app.injector.instanceOf[PasCisSource]

  "createCisAppeal" should {

    "return CreateCisAppealResponse" in {

      val result = repository.createCisAppeal(CreateCisAppealRequest(1L)).futureValue

      result mustBe CreateCisAppealResponse(true)
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.createCisAppeal(CreateCisAppealRequest(500L)).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }
  }

  "getCisAppealablePenalties" should {

    "return GetCisAppealablePenaltiesResponse when 2 items in List" in {

      val request = GetCisAppealablePenaltiesRequest(
        hmrcOfficeNumber = "123",
        schemeReferenceNumber = "AB456",
        startRow = 1L,
        endRow = 10L
      )

      val expectedResponse = GetCisAppealablePenaltiesResponse(
        total = 2,
        appealablePenalties = List(
          GetCisAppealablePenaltyResponse(
            uniqueId = Some(10001),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType = Some("CIS03"),
            penaltyAmount = Some(BigDecimal(999.23))
          ),
          GetCisAppealablePenaltyResponse(
            uniqueId = Some(10002),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType = Some("CIS03"),
            penaltyAmount = Some(BigDecimal(999.23))
          )
        )
      )

      val result = repository.getCisAppealablePenalties(request).futureValue

      result mustBe expectedResponse
    }

    "return GetCisAppealablePenaltiesResponse when no items in List" in {

      val request = GetCisAppealablePenaltiesRequest(
        hmrcOfficeNumber = "456",
        schemeReferenceNumber = "AB456",
        startRow = 1L,
        endRow = 10L
      )

      val expectedResponse = GetCisAppealablePenaltiesResponse(
        total = 0,
        appealablePenalties = List.empty
      )

      val result = repository.getCisAppealablePenalties(request).futureValue

      result mustBe expectedResponse
    }

    "propagate downstream failure from stub" in {
      val request = GetCisAppealablePenaltiesRequest(
        hmrcOfficeNumber = "789",
        schemeReferenceNumber = "AB456",
        startRow = 1L,
        endRow = 10L
      )

      val exception = intercept[Error] {
        repository.getCisAppealablePenalties(request).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }

  "getCisAppealablePenalty" should {

    "return GetCisAppealablePenaltyResponse" in {

      val result = repository.getCisAppealablePenalty(
        GetCisAppealablePenaltyRequest(
          penaltyId = 1L,
          hmrcOfficeNumber = "840123",
          schemeReferenceNumber = "098345"
        )).futureValue

      result mustBe GetCisAppealablePenaltyResponse(
        uniqueId = None,
        issueDate = Some(LocalDate.of(2023, 1, 1)),
        taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
        penaltyType = Some("CIS003"),
        penaltyAmount = Some(BigDecimal(999.23))
      )
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getCisAppealablePenalty(
          GetCisAppealablePenaltyRequest(
            penaltyId = 500L,
            hmrcOfficeNumber = "840123",
            schemeReferenceNumber = "098345"
          )).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }
  }

  "getCisPenaltyAllowed" should {

    "return GetCisPenaltyAllowedResponse" in {

      val result = repository.getCisPenaltyAllowed(
        GetCisPenaltyAllowedRequest(
          districtNumber = 1L,
          employerReference = "840123",
          uniqueId = 1L
        )).futureValue

      result mustBe GetCisPenaltyAllowedResponse(
        allowed = true
      )
    }


    "propagate downstream failure from stub" in {
      val request = GetCisPenaltyAllowedRequest(
        districtNumber = 2L,
        employerReference = "AB456",
        uniqueId = 2L
      )

      val exception = intercept[Error] {
        repository.getCisPenaltyAllowed(request).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }
}