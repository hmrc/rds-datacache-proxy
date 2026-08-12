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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.APBalancedItem

trait AccountingPeriodDetailsStubData {

  val aPBalancedItemDefault = APBalancedItem(
    isApBalanced              = Some("N"),
    lpiCalcFlag               = Some("N"),
    crDbCalcFlag              = Some("Y"),
    creditInterestAmount      = Some(BigDecimal(101.161)),
    debitInterestAmount       = Some(BigDecimal(191.7891)),
    latePaymentInterestAmount = Some(BigDecimal(301.563)),
    repaymentInterestAmount   = Some(BigDecimal(401.3236)),
    amountDueForAp            = Some(BigDecimal(501.896))
  )

  val aPBalancedItemEmpty = APBalancedItem(
    isApBalanced              = None,
    lpiCalcFlag               = None,
    crDbCalcFlag              = None,
    creditInterestAmount      = None,
    debitInterestAmount       = None,
    latePaymentInterestAmount = None,
    repaymentInterestAmount   = None,
    amountDueForAp            = None
  )

  def getIsAPBalancedData(taxRef: Long, accPeriod: Long): APBalancedItem = {
    (taxRef, accPeriod) match {
      case (1, 1) =>
        aPBalancedItemDefault
      case (100, _) =>
        aPBalancedItemEmpty
      case (_, _) =>
        throw new Error("Simulated downstream failure")
    }
  }
}
