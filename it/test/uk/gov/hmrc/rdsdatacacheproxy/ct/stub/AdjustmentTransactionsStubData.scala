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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdjustmentTransactions

object AdjustmentTransactionsStubData {

  val adjustmentTransactions: List[AdjustmentTransactions] =
    List(
      AdjustmentTransactions(
        amount = BigDecimal(10),
        `type` = "N"
      ),
      AdjustmentTransactions(
        amount = BigDecimal(20),
        `type` = "F"
      ),
      AdjustmentTransactions(
        amount = BigDecimal(30),
        `type` = "L"
      )
    )

  val emptyAdjustmentTransactions: List[AdjustmentTransactions] = List.empty

  def getAdjustmentTransactions(taxRef: Long, accPeriod: Long): List[AdjustmentTransactions] = {
    taxRef match {
      case 10L  => adjustmentTransactions
      case 200L => throw new RuntimeException("Downstream error")
      case _    => emptyAdjustmentTransactions
    }
  }

}
