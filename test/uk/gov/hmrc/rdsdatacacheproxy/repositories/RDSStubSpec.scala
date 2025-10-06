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

package uk.gov.hmrc.rdsdatacacheproxy.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.PaymentPlanDuplicateCheckRequest
import uk.gov.hmrc.rdsdatacacheproxy.utils.StubUtils
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDIReference, DirectDebit, DuplicateCheckResponse, EarliestPaymentDate, UserDebits}

import java.time.{LocalDate, LocalDateTime}
import scala.collection.immutable.Seq

class RDSStubSpec
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
    }
  }
  def expected(i: Int): DirectDebit = connector.stubData.randomDirectDebit(i)

  "RDSStub" should:
    "return a DirectDebit" in:
      val result = connector.getDirectDebits("123").futureValue

      result shouldBe UserDebits(5, Seq(expected(1), expected(2), expected(3), expected(4), expected(5)))

    "return earliest payment date" in :
      val result = connector.addFutureWorkingDays(LocalDate.of(2025, 12, 15), 10).futureValue

      result shouldBe EarliestPaymentDate(LocalDate.of(2025, 12, 25))

    "return ddi reference number" in :
      val result = connector.getDirectDebitReference("xyz", "000123", "session-123").futureValue

      result shouldBe DDIReference("xyz")

    "return true if duplicate payment plan" in {
      val currentTime = LocalDateTime.now().withNano(0)

      val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
        directDebitReference = "testRef",
        paymentPlanReference = "payment ref 123",
        planType = "type 1",
        paymentService = "CESA",
        paymentReference = "payment ref",
        paymentAmount = 120.00,
        totalLiability = 780.00,
        paymentFrequency = "1"
      )

      val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("dd reference", "0000000009000201", duplicateCheckRequest).futureValue

      result shouldBe DuplicateCheckResponse(true)
    }

    "return false if not a duplicate payment plan" in {
        val currentTime = LocalDateTime.now().withNano(0)

        val duplicateCheckRequest: PaymentPlanDuplicateCheckRequest = PaymentPlanDuplicateCheckRequest(
          directDebitReference = "testRef",
          paymentPlanReference = "payment ref 123",
          planType = "type 1",
          paymentService = "CESA",
          paymentReference = "payment ref",
          paymentAmount = 120.00,
          totalLiability = 780.00,
          paymentFrequency = "WEEKLY"
        )

        val result: DuplicateCheckResponse = connector.isDuplicatePaymentPlan("dd reference", "0000000009000202", duplicateCheckRequest).futureValue

        result shouldBe DuplicateCheckResponse(false)
      }
