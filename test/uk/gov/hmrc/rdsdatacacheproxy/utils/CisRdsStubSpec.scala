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

package uk.gov.hmrc.rdsdatacacheproxy.utils

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.models.{MonthlyReturn, UserMonthlyReturns}

import java.time.LocalDateTime
import scala.concurrent.Future

class CisRdsStubSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures {

  private def mkReturn(
                        id: Long,
                        month: Int,
                        year: Int = 2025
                      ): MonthlyReturn =
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
      lastUpdate             = Some(LocalDateTime.parse("2025-01-01T00:00:00")),
      amendment              = Some("N"),
      supersededBy           = None
    )

  private class TestableCisRdsStub(rows: Seq[MonthlyReturn]) extends CisRdsStub(new StubUtils) {
    override def getMonthlyReturns(instanceId: String): Future[UserMonthlyReturns] =
      Future.successful(UserMonthlyReturns(rows))
  }

  "CisRdsStub.findInstanceId" should {
    "return Some(\"1\") when TON and TOR are non-empty" in {
      val connector = new CisRdsStub(new StubUtils)
      whenReady(connector.findInstanceId("123", "ABC")) { res =>
        res shouldBe Some("1")
      }
    }

    "return None when TON is empty" in {
      val connector = new CisRdsStub(new StubUtils)
      whenReady(connector.findInstanceId("   ", "ABC")) { res =>
        res shouldBe None
      }
    }

    "return None when TOR is empty" in {
      val connector = new CisRdsStub(new StubUtils)
      whenReady(connector.findInstanceId("123", "   ")) { res =>
        res shouldBe None
      }
    }

    "return None when both are null" in {
      val connector = new CisRdsStub(new StubUtils)
      whenReady(connector.findInstanceId(null, null)) { res =>
        res shouldBe None
      }
    }
  }

  "CisRdsStub.getMonthlyReturns" should {
    "return 3 rows, each with a unique month and year" in {
      val expected = Seq(
        mkReturn(id = 1L, month = 1),
        mkReturn(id = 2L, month = 2),
        mkReturn(id = 3L, month = 3)
      )
      val connector = new TestableCisRdsStub(expected)

      whenReady(connector.getMonthlyReturns("test123")) { res =>
        res shouldBe UserMonthlyReturns(expected)
      }
    }

    "return empty MonthlyReturns when the stub returns none" in {
      val connector = new TestableCisRdsStub(Seq.empty)

      whenReady(connector.getMonthlyReturns("ABC123")) { res =>
        res shouldBe UserMonthlyReturns(Seq.empty)
      }
    }
  }
}