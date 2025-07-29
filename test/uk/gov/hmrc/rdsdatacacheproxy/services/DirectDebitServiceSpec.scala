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
import uk.gov.hmrc.rdsdatacacheproxy.models.{DirectDebit, UserDebits}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitServiceSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with IntegrationPatience:

  implicit val ec: ExecutionContext = global

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
        when(mockConnector.getDirectDebits(any(), any(), any()))
          .thenReturn(Future.successful(Seq()))

          val result = service.retrieveDirectDebits("testId", 1, 99).futureValue
          result shouldBe UserDebits(0, Seq())
      "retrieving Direct Debits" in:
        when(mockConnector.getDirectDebits(any(), any(), any()))
          .thenReturn(
            Future.successful(Seq(expected(1))),
            Future.successful(Seq(expected(2), expected(3), expected(4))),
          )

          val result = service.retrieveDirectDebits("testId", 1, 99).futureValue
          result shouldBe UserDebits(1, Seq(expected(1)))
          val result2 = service.retrieveDirectDebits("testId", 1, 99).futureValue
          result2 shouldBe UserDebits(3, Seq(expected(2),expected(3),expected(4)))

    "fail" when:
      "retrieving Direct Debits" in:
        when(mockConnector.getDirectDebits(any(), any(), any()))
          .thenReturn(Future.failed(new Exception("bang")))

        val result = intercept[Exception](service.retrieveDirectDebits("testId", 1, -1).futureValue)
        result.getMessage should include("bang")
