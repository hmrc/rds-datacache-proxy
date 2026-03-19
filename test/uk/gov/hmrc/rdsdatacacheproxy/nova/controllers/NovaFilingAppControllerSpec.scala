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
import uk.gov.hmrc.rdsdatacacheproxy.nova.models.*
import uk.gov.hmrc.rdsdatacacheproxy.nova.repositories.NovaDataSource

import java.time.LocalDate
import scala.concurrent.Future

class NovaFilingAppControllerSpec extends SpecBase {

  // ─── Test data ───────────────────────────────────────────────────────────────

  private val testTraderInfo = TraderInformation(
    vrn                   = "123456789",
    status                = Some("REGD"),
    traderName            = Some("ABC Ltd"),
    tradingName           = Some("ABC Trading"),
    addressLine1          = Some("1 Test Street"),
    addressLine2          = Some("Testville"),
    addressLine3          = None,
    addressLine4          = None,
    postcode              = Some("SW1A 1AA"),
    email                 = Some("abc@example.com"),
    organisationType      = Some("LIMITED_COMPANY"),
    tradeClass            = Some("47"),
    tradeClassDescription = Some("Retail trade"),
    effectiveRegDate      = Some("2000-01-01"),
    ceasedDate            = None,
    certIssuedDate        = Some("2000-01-10"),
    nextReturnPeDate      = Some("2026-03-31"),
    returnStagger         = Some("MAR")
  )

  private val testVehicle = VehicleStatusDetails(
    vin             = "WBA12345678901234",
    novaRef         = Some("NOVA26E100001"),
    make            = Some("BMW"),
    model           = Some("3 Series"),
    mileage         = Some(5000),
    firstRegDate    = Some("2024-01-15"),
    secured         = true,
    restrictionDate = None,
    imported        = true
  )

  private val testCalcData = VehicleCalculationData(
    exchangeRate         = Some(BigDecimal("1.1523")),
    vatRateEffectiveDate = Some("2011-01-04"),
    vatRate              = Some(BigDecimal("20.0")),
    minLimitEffDate      = Some("2012-04-01"),
    minLimitAmount       = Some(BigDecimal("50.00")),
    thresholdDaysEffDate = Some("2012-04-01"),
    thresholdDays        = Some(14),
    rateEffDate          = Some("2012-04-01"),
    rateAmount           = Some(BigDecimal("5.00")),
    maxNoOfDaysEffDate   = Some("2012-04-01"),
    maxNoOfDays          = Some(100),
    altAmtEffDate        = Some("2012-04-01"),
    altAmt               = Some(BigDecimal("500.00"))
  )

  private val testEuStates = EuMemberStatesResponse(
    List(
      EuMemberState("DE", "Germany", Some("1958-01-01"), None, None),
      EuMemberState("FR", "France", Some("1958-01-01"), None, None)
    )
  )

  private val testNvraFound = NvraKnownFacts(
    nvraRefNumber = "NVRA123456",
    agentName     = Some("Test Agent Ltd"),
    addressLine1  = Some("1 Agent Street"),
    addressLine2  = Some("Agentville"),
    addressLine3  = None,
    addressLine4  = None,
    addressLine5  = None,
    postcode      = Some("AG1 1NT"),
    abroadFlag    = Some("N"),
    resultCode    = "000"
  )

  private val testNvraNotFound = NvraKnownFacts(
    nvraRefNumber = "NVRA999999",
    agentName     = None,
    addressLine1  = None,
    addressLine2  = None,
    addressLine3  = None,
    addressLine4  = None,
    addressLine5  = None,
    postcode      = None,
    abroadFlag    = None,
    resultCode    = "001"
  )

  // ─── getTraderInformation ────────────────────────────────────────────────────

  "NovaFilingAppController#getTraderInformation" - {

    "return 200 with trader information when trader is found" in new SetUp {
      when(mockNovaDataSource.getTraderInformation(any[String], any[Option[Int]]))
        .thenReturn(Future.successful(Some(testTraderInfo)))

      val result: Future[Result] = controller.getTraderInformation("123456789", None)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testTraderInfo)
      verify(mockNovaDataSource).getTraderInformation("123456789", None)
    }

    "return 200 with trader information when gracePeriod is provided" in new SetUp {
      when(mockNovaDataSource.getTraderInformation(any[String], any[Option[Int]]))
        .thenReturn(Future.successful(Some(testTraderInfo)))

      val result: Future[Result] = controller.getTraderInformation("123456789", Some(7200))(fakeRequest)

      status(result) mustBe OK
      verify(mockNovaDataSource).getTraderInformation("123456789", Some(7200))
    }

    "return 404 when trader is not found" in new SetUp {
      when(mockNovaDataSource.getTraderInformation(any[String], any[Option[Int]]))
        .thenReturn(Future.successful(None))

      val result: Future[Result] = controller.getTraderInformation("123456789", None)(fakeRequest)

      status(result) mustBe NOT_FOUND
      (contentAsJson(result) \ "code").as[String] mustBe "TRADER_NOT_FOUND"
    }

    "return 400 when vrn is empty" in new SetUp {
      val result: Future[Result] = controller.getTraderInformation("", None)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] mustBe "vrn must be provided"
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 400 when vrn is whitespace only" in new SetUp {
      val result: Future[Result] = controller.getTraderInformation("   ", None)(fakeRequest)

      status(result) mustBe BAD_REQUEST
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getTraderInformation(any[String], any[Option[Int]]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getTraderInformation("123456789", None)(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve trader information"
    }
  }

  // ─── getVehicleStatus ───────────────────────────────────────────────────────

  "NovaFilingAppController#getVehicleStatus" - {

    "return 200 with vehicle details when vehicle is found" in new SetUp {
      when(mockNovaDataSource.getVehicleStatusDetails(any[String]))
        .thenReturn(Future.successful(Some(testVehicle)))

      val result: Future[Result] = controller.getVehicleStatus("WBA12345678901234")(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testVehicle)
      verify(mockNovaDataSource).getVehicleStatusDetails("WBA12345678901234")
    }

    "return 404 when vehicle is not found" in new SetUp {
      when(mockNovaDataSource.getVehicleStatusDetails(any[String]))
        .thenReturn(Future.successful(None))

      val result: Future[Result] = controller.getVehicleStatus("WBA12345678901234")(fakeRequest)

      status(result) mustBe NOT_FOUND
      (contentAsJson(result) \ "code").as[String] mustBe "VEHICLE_NOT_FOUND"
    }

    "return 400 when vin is empty" in new SetUp {
      val result: Future[Result] = controller.getVehicleStatus("")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] mustBe "vin must be provided"
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 400 when vin is whitespace only" in new SetUp {
      val result: Future[Result] = controller.getVehicleStatus("   ")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getVehicleStatusDetails(any[String]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getVehicleStatus("WBA12345678901234")(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve vehicle status"
    }
  }

  // ─── getVehicleCalculationData ───────────────────────────────────────────────

  "NovaFilingAppController#getVehicleCalculationData" - {

    "return 200 with calculation data" in new SetUp {
      when(mockNovaDataSource.getVehicleCalculationData(any[String], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.successful(testCalcData))

      val result: Future[Result] =
        controller.getVehicleCalculationData("EUR", "2024-01-15", "2024-02-01")(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testCalcData)
      verify(mockNovaDataSource).getVehicleCalculationData(
        "EUR",
        LocalDate.parse("2024-01-15"),
        LocalDate.parse("2024-02-01")
      )
    }

    "return 400 when invoiceDate is not a valid date" in new SetUp {
      val result: Future[Result] =
        controller.getVehicleCalculationData("EUR", "not-a-date", "2024-02-01")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] must include("invoiceDate")
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 400 when arrivalDate is not a valid date" in new SetUp {
      val result: Future[Result] =
        controller.getVehicleCalculationData("EUR", "2024-01-15", "bad")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] must include("arrivalDate")
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getVehicleCalculationData(any[String], any[LocalDate], any[LocalDate]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] =
        controller.getVehicleCalculationData("EUR", "2024-01-15", "2024-02-01")(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve vehicle calculation data"
    }
  }

  // ─── getEuMemberStates ───────────────────────────────────────────────────────

  "NovaFilingAppController#getEuMemberStates" - {

    "return 200 with EU member states" in new SetUp {
      when(mockNovaDataSource.getEuMemberStates())
        .thenReturn(Future.successful(testEuStates))

      val result: Future[Result] = controller.getEuMemberStates()(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testEuStates)
      verify(mockNovaDataSource).getEuMemberStates()
    }

    "return 200 with empty list when no member states exist" in new SetUp {
      when(mockNovaDataSource.getEuMemberStates())
        .thenReturn(Future.successful(EuMemberStatesResponse(List.empty)))

      val result: Future[Result] = controller.getEuMemberStates()(fakeRequest)

      status(result) mustBe OK
      (contentAsJson(result) \ "euMemberStates").as[List[EuMemberState]] mustBe List.empty
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getEuMemberStates())
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getEuMemberStates()(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve EU member states"
    }
  }

  // ─── getNvraKnownFacts ───────────────────────────────────────────────────────

  "NovaFilingAppController#getNvraKnownFacts" - {

    "return 200 with NVRA data when resultCode is 000 (data found)" in new SetUp {
      when(mockNovaDataSource.getNvraKnownFacts(any[String]))
        .thenReturn(Future.successful(testNvraFound))

      val result: Future[Result] = controller.getNvraKnownFacts("NVRA123456")(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(testNvraFound)
      verify(mockNovaDataSource).getNvraKnownFacts("NVRA123456")
    }

    "return 200 with resultCode 001 when no NVRA record is found" in new SetUp {
      when(mockNovaDataSource.getNvraKnownFacts(any[String]))
        .thenReturn(Future.successful(testNvraNotFound))

      val result: Future[Result] = controller.getNvraKnownFacts("NVRA999999")(fakeRequest)

      status(result) mustBe OK
      (contentAsJson(result) \ "resultCode").as[String] mustBe "001"
    }

    "return 400 when nvraRefNumber is empty" in new SetUp {
      val result: Future[Result] = controller.getNvraKnownFacts("")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] mustBe "nvraRefNumber must be provided"
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 400 when nvraRefNumber is whitespace only" in new SetUp {
      val result: Future[Result] = controller.getNvraKnownFacts("   ")(fakeRequest)

      status(result) mustBe BAD_REQUEST
      verifyNoInteractions(mockNovaDataSource)
    }

    "return 500 when the repository call fails" in new SetUp {
      when(mockNovaDataSource.getNvraKnownFacts(any[String]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getNvraKnownFacts("NVRA123456")(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      (contentAsJson(result) \ "error").as[String] mustBe "Failed to retrieve NVRA known facts"
    }
  }

  private class SetUp {
    val mockNovaDataSource: NovaDataSource = mock[NovaDataSource]
    val controller: NovaFilingAppController = new NovaFilingAppController(fakeAuthAction, mockNovaDataSource, cc)
  }
}
