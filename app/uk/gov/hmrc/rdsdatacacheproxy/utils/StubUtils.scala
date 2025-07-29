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

import uk.gov.hmrc.rdsdatacacheproxy.models.DirectDebit

import java.time.LocalDateTime
import scala.util.Random

class StubUtils {

  private def r(range: Int) = {
    val random = s"${Random.nextInt(range) + 1}"
    val prepend = if (random.length == 1) "0" else ""
    prepend + random
  }

  def randomDirectDebit(i: Int): DirectDebit = {
    val date = s"${Random.nextInt(5) + 2022}" +
      s"-${r(12)}" +
      s"-${r(28)}"
    DirectDebit.apply(
      ddiRefNumber = s"defaultRef$i",
      LocalDateTime.parse(s"${date}T00:00:00"),
      s"${r(99)}-${r(99)}-${r(99)}",
      Seq.fill(8)(Random.nextInt(10)).mkString,
      "BankLtd",
      Random.nextBoolean(),
      Random.nextInt(100)
    )
  }
}
