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

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.models.{DirectDebit, DirectDebits}

class DirectDebitServiceSpec
  extends AnyWordSpec
     with Matchers
     with ScalaFutures
     with IntegrationPatience:

  private val service  = new DirectDebitService
  val expected: DirectDebit = service.defaultDD

  "DirectDebitService" should:
    "succeed" when:
      "retrieving Direct Debits" in:
        val expectedResults = Seq(
          Seq(),
          Seq(expected),
          Seq(expected, expected, expected)
        )

        for (cases <- expectedResults)
          val result = service.retrieveDirectDebits("testId").futureValue
          result shouldBe DirectDebits(List(expected))

      "retrieving Direct Debits with an offset" in:
        val result = service.retrieveDirectDebitsWithOffset("testId", "2020-02-02", 1).futureValue
        result shouldBe DirectDebits(List(expected))
    "fail" when:
      "offset is not a valid date" ignore:
        val result = intercept[Exception](service.retrieveDirectDebitsWithOffset("testId", "Pancake Day", 1))
        result.getMessage shouldBe "Invalid date provided for offset"

      "retrieving Direct Debits" ignore:
        val result = service.retrieveDirectDebits("testId").futureValue
        result shouldBe DirectDebits(List(expected))

      "retrieving Direct Debit" ignore:
        val result = service.retrieveDirectDebits("testId").futureValue
        result shouldBe DirectDebits(List(expected))
