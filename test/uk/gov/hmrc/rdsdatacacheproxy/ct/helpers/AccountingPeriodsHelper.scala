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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{RdsAccountingPeriod, RdsAccountingPeriodsRowResponse}

import java.time.LocalDate

trait AccountingPeriodsHelper {

  val emptyAccountingPeriods: RdsAccountingPeriod = RdsAccountingPeriod(accountingPeriods = List.empty)
  val accountingPeriodsWithSingleItem: RdsAccountingPeriod = RdsAccountingPeriod(accountingPeriods =
    List(
      RdsAccountingPeriodsRowResponse(
        accountingPeriod       = Some(BigDecimal(202501)),
        apStartDate            = Some(LocalDate.of(2025, 1, 1)),
        apEndDate              = Some(LocalDate.of(2025, 12, 31)),
        apStatus               = Some("Open"),
        taxChargePresent       = Some("Y"),
        clericalIntSig         = Some("Y"),
        creditDebitInterestInd = Some("Y"),
        taxTotal               = Some(BigDecimal(12345.67)),
        interestTotal          = Some(BigDecimal(89.10)),
        penaltyTotal           = Some(BigDecimal(250.00)),
        payslipTotal           = Some(BigDecimal(5000.00)),
        repayReallocTotal      = Some(BigDecimal(300.00)),
        adjustmentTotal        = Some(BigDecimal(75.50))
      )
    )
  )
  val accountingPeriodsWithMultipleItems: RdsAccountingPeriod = RdsAccountingPeriod(accountingPeriods =
    List(
      RdsAccountingPeriodsRowResponse(
        accountingPeriod       = Some(BigDecimal(202501)),
        apStartDate            = Some(LocalDate.of(2025, 1, 1)),
        apEndDate              = Some(LocalDate.of(2025, 12, 31)),
        apStatus               = Some("Open"),
        taxChargePresent       = Some("Y"),
        clericalIntSig         = Some("Y"),
        creditDebitInterestInd = Some("Y"),
        taxTotal               = Some(BigDecimal(12345.67)),
        interestTotal          = Some(BigDecimal(89.10)),
        penaltyTotal           = Some(BigDecimal(250.00)),
        payslipTotal           = Some(BigDecimal(5000.00)),
        repayReallocTotal      = Some(BigDecimal(300.00)),
        adjustmentTotal        = Some(BigDecimal(75.50))
      ),
      RdsAccountingPeriodsRowResponse(
        accountingPeriod       = Some(BigDecimal(202501)),
        apStartDate            = Some(LocalDate.of(2025, 1, 1)),
        apEndDate              = Some(LocalDate.of(2025, 12, 31)),
        apStatus               = Some("Open"),
        taxChargePresent       = Some("Y"),
        clericalIntSig         = Some("N"),
        creditDebitInterestInd = Some("N"),
        taxTotal               = Some(BigDecimal(12345.67)),
        interestTotal          = Some(BigDecimal(89.10)),
        penaltyTotal           = Some(BigDecimal(250.00)),
        payslipTotal           = Some(BigDecimal(5000.00)),
        repayReallocTotal      = Some(BigDecimal(300.00)),
        adjustmentTotal        = Some(BigDecimal(75.50))
      ),
      RdsAccountingPeriodsRowResponse(
        accountingPeriod       = Some(BigDecimal(202501)),
        apStartDate            = Some(LocalDate.of(2025, 1, 1)),
        apEndDate              = Some(LocalDate.of(2025, 12, 31)),
        apStatus               = Some("Open"),
        taxChargePresent       = Some("N"),
        clericalIntSig         = Some("Y"),
        creditDebitInterestInd = Some("N"),
        taxTotal               = Some(BigDecimal(12345.67)),
        interestTotal          = Some(BigDecimal(89.10)),
        penaltyTotal           = Some(BigDecimal(250.00)),
        payslipTotal           = Some(BigDecimal(5000.00)),
        repayReallocTotal      = Some(BigDecimal(300.00)),
        adjustmentTotal        = Some(BigDecimal(75.50))
      )
    )
  )

}
