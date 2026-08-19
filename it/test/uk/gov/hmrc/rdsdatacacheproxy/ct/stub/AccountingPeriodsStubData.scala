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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{AccountingPeriods, AccountingPeriodsInfo}

import java.time.LocalDate


object AccountingPeriodsStubData {

  val accountingPeriodsWithOneItem: AccountingPeriods = AccountingPeriods(
    List(
      AccountingPeriodsInfo(
        accountingPeriod = BigDecimal(1),
        apStartDate = LocalDate.of(2027, 7, 24),
        apEndDate = LocalDate.of(2028, 7, 24),
        apStatus = "S",
        taxChargePresent = true,
        clericalIntSig = false,
        creditDebitInterestInd = true,
        taxTotal = Some(BigDecimal(10)),
        interestTotal = Some(BigDecimal(20)),
        penaltyTotal = Some(BigDecimal(30)),
        payslipTotal = Some(BigDecimal(40)),
        repayReallocTotal = Some(BigDecimal(50)),
        adjustmentTotal = Some(BigDecimal(60))
      )
    )
  )

  val accountingPeriodsWithMultipleItems: AccountingPeriods = AccountingPeriods(
    List(
      AccountingPeriodsInfo(
        accountingPeriod = BigDecimal(2),
        apStartDate = LocalDate.of(2027, 3, 5),
        apEndDate = LocalDate.of(2028, 3, 5),
        apStatus = "C",
        taxChargePresent = false,
        clericalIntSig = true,
        creditDebitInterestInd = false,
        taxTotal = Some(BigDecimal(100)),
        interestTotal = Some(BigDecimal(200)),
        penaltyTotal = Some(BigDecimal(300)),
        payslipTotal = Some(BigDecimal(400)),
        repayReallocTotal = Some(BigDecimal(500)),
        adjustmentTotal = Some(BigDecimal(600))
      ),
      AccountingPeriodsInfo(
        accountingPeriod = BigDecimal(3),
        apStartDate = LocalDate.of(2016, 11, 22),
        apEndDate = LocalDate.of(2017, 11, 22),
        apStatus = "P",
        taxChargePresent = true,
        clericalIntSig = true,
        creditDebitInterestInd = true,
        taxTotal = Some(BigDecimal(110)),
        interestTotal = Some(BigDecimal(220)),
        penaltyTotal = Some(BigDecimal(330)),
        payslipTotal = Some(BigDecimal(440)),
        repayReallocTotal = Some(BigDecimal(550)),
        adjustmentTotal = Some(BigDecimal(660))
      )
    )
  )

  val emptyAccountingPeriods: AccountingPeriods = AccountingPeriods(List.empty)


  def getAccountPeriods(taxRef: Long): AccountingPeriods = {
    taxRef match {
      case 10L  => accountingPeriodsWithOneItem
      case 20L  => accountingPeriodsWithMultipleItems
      case 200L => throw new RuntimeException("Downstream error")
      case _    => emptyAccountingPeriods
    }
  }

}
