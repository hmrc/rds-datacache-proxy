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

package uk.gov.hmrc.rdsdatacacheproxy.ct.stub.payments

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{AllAccountingPeriods, AccountingPeriodList}

import java.time.LocalDate


object AllAccountingPeriodsStubData {

  val emptyAllAccountingPeriods: AllAccountingPeriods = AllAccountingPeriods(List.empty)

  val allAccountingPeriods: AllAccountingPeriods = AllAccountingPeriods(
    List(
      AccountingPeriodList(
        accountingPeriod = BigDecimal(10),
        apEndDate = LocalDate.of(2025, 7, 24),
        taxChargePresent = "true",
        penaltyTotal = BigDecimal(10)
      )
    )
  )

  val multipleAllAccountingPeriods: AllAccountingPeriods = AllAccountingPeriods(
    List(
      AccountingPeriodList(
        accountingPeriod = BigDecimal(20),
        apEndDate = LocalDate.of(2025, 8, 24),
        taxChargePresent = "true",
        penaltyTotal = BigDecimal(20)
      ),
      AccountingPeriodList(
        accountingPeriod = BigDecimal(30),
        apEndDate = LocalDate.of(2025, 8, 24),
        taxChargePresent = "false",
        penaltyTotal = BigDecimal(30)
      )
    )
  )

  def getAccountingPeriodList(taxRef: Long): AllAccountingPeriods = {
    taxRef match {
      case 10L => allAccountingPeriods
      case 20L => multipleAllAccountingPeriods
      case 30L => emptyAllAccountingPeriods
      case _ => throw new RuntimeException("Downstream error")
    }
  }


}
