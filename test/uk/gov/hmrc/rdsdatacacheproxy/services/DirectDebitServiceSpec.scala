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

package uk.gov.hmrc.rdsdatacacheproxy.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.connectors.RDSConnector
import uk.gov.hmrc.rdsdatacacheproxy.models.DirectDebit

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class DirectDebitServiceSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with IntegrationPatience:

  private val mockConnector = mock[RDSConnector]
  private val service = new DirectDebitService(mockConnector)

  def expected(i: Int): DirectDebit =
    DirectDebit.apply(
      s"defaultRef$i",
      LocalDateTime.parse("2020-02-02T22:22:22"),
      "00-00-00",
      "00000000",
      "Bank Ltd",
      false,
      i
    )


  "DirectDebitService" should:
    "succeed" when:
      "retrieving no Direct Debits" in:
        when(mockConnector.getDirectDebits(any(), any[None.type](), any[None.type]()))
          .thenReturn(Future.successful(List()))

          val result = service.retrieveDirectDebits("testId").futureValue
          result shouldBe List()
      "retrieving Direct Debits" in:
        when(mockConnector.getDirectDebits(any(), any[None.type](), any[None.type]()))
          .thenReturn(Future.successful(List(expected(1))))

          val result = service.retrieveDirectDebits("testId").futureValue
          result shouldBe List(expected(1))

      "retrieving a Direct Debit with an offset" in:
        when(mockConnector.getDirectDebits(any(), any[Some[LocalDate]](), any[Some[Int]]()))
          .thenReturn(Future.successful(List(expected(1))))

        val result = service.retrieveDirectDebitsWithOffset("testId", "2020-02-02", 1).futureValue
        result shouldBe List(expected(1))

      "retrieving multiple Direct Debits with an offset" in:
        when(mockConnector.getDirectDebits(any(), any[Some[LocalDate]](), any[Some[Int]]()))
          .thenReturn(Future.successful(List(expected(1), expected(2), expected(3), expected(4))))

        val result = service.retrieveDirectDebitsWithOffset("testId", "2020-02-02", 4).futureValue
        result shouldBe List(expected(1), expected(2), expected(3), expected(4))

    "fail" when:
      "offset is not a valid date" in:
        val result = intercept[Exception](service.retrieveDirectDebitsWithOffset("testId", "Pancake Day", 1).futureValue)
        result.getMessage should include("Invalid date provided for offset")

      "retrieving Direct Debits" in:
        when(mockConnector.getDirectDebits(any(), any[Some[LocalDate]](), any[Some[Int]]()))
          .thenReturn(Future.failed(new Exception("bang")))

        val result = intercept[Exception](service.retrieveDirectDebits("testId").futureValue)
        result.getMessage should include("bang")
