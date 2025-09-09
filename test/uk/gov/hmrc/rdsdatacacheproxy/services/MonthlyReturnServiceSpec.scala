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
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.models.{MonthlyReturn, UserMonthlyReturns}
import uk.gov.hmrc.rdsdatacacheproxy.utils.CisRdsStub

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global

class MonthlyReturnServiceSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with IntegrationPatience
    with BeforeAndAfterEach:

  implicit val ec: ExecutionContext = global

  private val mockConnector = mock[CisRdsStub]
  private val service = new MonthlyReturnService(mockConnector)

  override def beforeEach(): Unit =
    super.beforeEach()
    reset(mockConnector)
    when(mockConnector.findInstanceId(any[String](), any[String]()))
      .thenReturn(Future.successful(Some("1")))

  private val fixedTs = LocalDateTime.parse("2025-01-01T00:00:00")
  private def mr(id: Long, year: Int, month: Int): MonthlyReturn =
    MonthlyReturn(
      monthlyReturnId        = id,
      taxYear                = year,
      taxMonth               = month,
      nilReturnIndicator     = Some("N"),
      decEmpStatusConsidered = Some("Y"),
      decAllSubsVerified     = Some("Y"),
      decInformationCorrect  = Some("Y"),
      decNoMoreSubPayments   = Some("N"),
      decNilReturnNoPayments = Some("N"),
      status                 = Some("SUBMITTED"),
      lastUpdate             = Some(fixedTs),
      amendment              = Some("N"),
      supersededBy           = None
    )

  "MonthlyReturnService" should:
    "succeed" when:
      "retrieving no Monthly Returns" in:
        when(mockConnector.getMonthlyReturns(any[String]()))
          .thenReturn(Future.successful(UserMonthlyReturns(Seq.empty)))

        val result = service.retrieveMonthlyReturns("123","AB456").futureValue
        result shouldBe UserMonthlyReturns(Seq.empty)

      "retrieving Monthly Returns" in:
        when(mockConnector.getMonthlyReturns(any[String]()))
          .thenReturn(
            Future.successful(UserMonthlyReturns(Seq(mr(1, 2025, 1)))),
            Future.successful(UserMonthlyReturns(Seq(mr(2, 2025, 7), mr(3, 2025, 7))))
          )

        val r1 = service.retrieveMonthlyReturns("123","AB456").futureValue
        r1 shouldBe UserMonthlyReturns(Seq(mr(1, 2025, 1)))

        val r2 = service.retrieveMonthlyReturns("123","AB456").futureValue
        r2 shouldBe UserMonthlyReturns(Seq(mr(2, 2025, 7), mr(3, 2025, 7)))

    "fail" when:
      "the connector errors" in:
        when(mockConnector.getMonthlyReturns(any[String]()))
          .thenReturn(Future.failed(new Exception("bang")))

        val ex = intercept[Exception] {
          service.retrieveMonthlyReturns("123","AB456").futureValue
        }
        ex.getMessage should include ("bang")

