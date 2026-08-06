/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.ct.stub

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{ReallocationFromAccDetails, ReallocationFromAccPeriod}

import java.time.LocalDate

object ReallocationFromAccPeriodStubData {

  val reallocationFromAccPeriod: ReallocationFromAccPeriod = ReallocationFromAccPeriod(
    List(
      ReallocationFromAccDetails(
        Some(BigDecimal(12390)),
        LocalDate.of(2026, 12, 27),
        Some(LocalDate.of(204, 2, 2)),
        "18969779586"
      ),
      ReallocationFromAccDetails(
        Some(BigDecimal(12345)),
        LocalDate.of(2026, 12, 27),
        Some(LocalDate.of(204, 2, 2)),
        "18969779586"
      )
    )
  )

  val emptyListReallocationFromAccPeriod: ReallocationFromAccPeriod = ReallocationFromAccPeriod(List.empty)

  def getReallocationFromAccPeriod(taxPayerReference: Long, accPeriod: Long): ReallocationFromAccPeriod = {
    (taxPayerReference, accPeriod) match {
      case (12L, 16L)     => reallocationFromAccPeriod
      case (9798L, 3786L) => throw new RuntimeException("Error from downstream")
      case (_, _)         => emptyListReallocationFromAccPeriod
    }
  }

}
