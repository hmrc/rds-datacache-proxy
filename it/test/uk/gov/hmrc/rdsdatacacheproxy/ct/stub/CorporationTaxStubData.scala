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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PenaltyTransaction
import java.time.LocalDate

object CorporationTaxStubData {

  val penaltiesEmptyList: List[PenaltyTransaction] = List.empty

  val penaltiesItems: List[PenaltyTransaction] = List(
    PenaltyTransaction(penaltyDate = LocalDate.of(2025, 5, 1), `type` = "F", postingAmount = BigDecimal(100.13)),
    PenaltyTransaction(penaltyDate = LocalDate.of(2021, 3, 7), `type` = "G", postingAmount = BigDecimal(27.19))
  )

  def getPenaltiesItems(taxRef: Long): List[PenaltyTransaction] = {
    taxRef match {
      case 1L  => penaltiesItems
      case 19L => throw new Error("Simulated downstream failure")
      case _ => penaltiesEmptyList
    }
  }

}
