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

package uk.gov.hmrc.rdsdatacacheproxy.connectors

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.models.{DirectDebit, MonthlyReturn, UserDebits, UserMonthlyReturns}
import uk.gov.hmrc.rdsdatacacheproxy.utils.StubUtils

import java.time.LocalDateTime
import scala.concurrent.Future

class RDSConnectorSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience:

  val connector: RdsStub = new RdsStub(){
    override val stubData: StubUtils = new StubUtils {
      override def randomDirectDebit(i: Int): DirectDebit =
        DirectDebit.apply(
          ddiRefNumber = s"defaultRef$i",
          LocalDateTime.parse("2020-02-02T22:22:22"),
          "00-00-00",
          "00000000",
          "BankLtd",
          false,
          i
        )

      override def randomMonthlyReturn(): MonthlyReturn =
        MonthlyReturn(
          monthlyReturnId        = 0L,
          taxYear                = 2025,
          taxMonth               = 1,
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
    }

    override def getMonthlyReturns(instanceId: String): Future[UserMonthlyReturns] = {
      val rows = Seq(
        stubData.randomMonthlyReturn().copy(monthlyReturnId = 1L, taxMonth = 1),
        stubData.randomMonthlyReturn().copy(monthlyReturnId = 2L, taxMonth = 7),
        stubData.randomMonthlyReturn().copy(monthlyReturnId = 3L, taxMonth = 7)
      )
      Future.successful(UserMonthlyReturns(rows))
    }
  }

  def expected(i: Int): DirectDebit = connector.stubData.randomDirectDebit(i)

  private def expectedMr(id: Long, month: Int): MonthlyReturn =
    connector.stubData.randomMonthlyReturn().copy(monthlyReturnId = id, taxMonth = month)

  "RDSConnector" should:
    "return a DirectDebit" in:
      val result = connector.getDirectDebits("123", 1, 1).futureValue

      result shouldBe UserDebits(1, Seq(expected(1)))

    "return DirectDebits up to a variable limit" in:
      val result1 = connector.getDirectDebits("123", 1, 3).futureValue
      val result2 = connector.getDirectDebits("123", 1, 5).futureValue

      result1 shouldBe UserDebits(3, Seq(expected(1), expected(2), expected(3)))
      result2 shouldBe UserDebits(5, Seq(expected(1), expected(2), expected(3), expected(4), expected(5)))

    "return MonthlyReturns (3 rows, with a duplicate month/year)" in:
      val res = connector.getMonthlyReturns("test123").futureValue
      res shouldBe UserMonthlyReturns(Seq(
        expectedMr(1L, 1),
        expectedMr(2L, 7),
        expectedMr(3L, 7)
      ))

    "return empty MonthlyReturns when stub returns none" in:
      val emptyConnector = new RdsStub() {
        override def getMonthlyReturns(instanceId: String): Future[UserMonthlyReturns] =
          Future.successful(UserMonthlyReturns(Seq.empty))
      }
      val res = emptyConnector.getMonthlyReturns("ABC123").futureValue
      res shouldBe UserMonthlyReturns(Seq.empty)
