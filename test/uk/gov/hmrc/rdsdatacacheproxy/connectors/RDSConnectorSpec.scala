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
import uk.gov.hmrc.rdsdatacacheproxy.models.DirectDebit

import java.time.LocalDate

class RDSConnectorSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience:

  val connector = new RDSConnector()
  def expected(i: Int): DirectDebit = connector.defaultDD(i)

  "RDSConnector" should:
    "return a DirectDebit" in:
      val result = connector.getDirectDebits("123").futureValue

      result shouldBe Seq(expected(1))

    "return DirectDebits up to a variable limit" in:
      val result1 = connector.getDirectDebits("123", Some(LocalDate.now()), Some(3)).futureValue
      val result2 = connector.getDirectDebits("123", Some(LocalDate.now()), Some(5)).futureValue

      result1 shouldBe Seq(expected(1), expected(2), expected(3))
      result2 shouldBe Seq(expected(1), expected(2), expected(3), expected(4), expected(5))
