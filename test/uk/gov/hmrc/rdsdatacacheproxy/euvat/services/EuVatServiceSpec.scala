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

package uk.gov.hmrc.rdsdatacacheproxy.euvat.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.euvat.models.responses.TradersKnownFacts
import uk.gov.hmrc.rdsdatacacheproxy.euvat.repositories.EuVatCacheRepository

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class EuVatServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar with IntegrationPatience:

  implicit val ec: ExecutionContext = global
  private val mockConnector = mock[EuVatCacheRepository]
  private val service = new EuVatService(mockConnector)

  val emptyKnownFactsResponse: TradersKnownFacts =
    TradersKnownFacts(0, None, None, None, None, None, None, None, None, Some(LocalDateTime.MIN), Some(LocalDateTime.MIN), None)

  val knownFactsResponse: TradersKnownFacts =
    TradersKnownFacts(
      123456789,
      Some("TestData"),
      Some("Line 1"),
      Some("Line 2"),
      Some("Line 3"),
      Some("Line 4"),
      Some("Line 5"),
      Some("NE3 9TG"),
      Some("7020"),
      Some(LocalDateTime.of(2025, 1, 11, 10, 38)),
      Some(LocalDateTime.of(2026, 1, 11, 10, 38)),
      Some("N")
    )

  "EuVatService" should:
    "succeed" when:
      "retrieving no Known facts" in:
        when(mockConnector.getTraderByVrn(any()))
          .thenReturn(Future.successful(Some(emptyKnownFactsResponse)))

        val result = service.retrieveTraderByVrn("123").futureValue
        result shouldBe Some(emptyKnownFactsResponse)

      "retrieving traders known facts" in:
        when(mockConnector.getTraderByVrn(any()))
          .thenReturn(Future.successful(Some(knownFactsResponse)))
        val result = service.retrieveTraderByVrn("123").futureValue
        result shouldBe Some(knownFactsResponse)

    "fail" when:
      "retrieving Direct Debits" in:
        when(mockConnector.getTraderByVrn(any()))
          .thenReturn(Future.failed(new Exception("bang")))

        val result = intercept[Exception](service.retrieveTraderByVrn("123").futureValue)
        result.getMessage should include("bang")
