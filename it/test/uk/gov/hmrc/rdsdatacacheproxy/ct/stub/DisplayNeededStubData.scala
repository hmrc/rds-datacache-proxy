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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.DisplayNeededItem

object DisplayNeededStubData {

  val displayNeededItemAllFalse: DisplayNeededItem = DisplayNeededItem(
    taxIsDisplayNeededFlag          = false,
    interestIsDisplayNeededFlag     = false,
    paymentIsDisplayNeededFlag      = false,
    repayReallocIsDisplayNeededFlag = false
  )

  val displayNeededItemAllTrue: DisplayNeededItem = DisplayNeededItem(
    taxIsDisplayNeededFlag          = true,
    interestIsDisplayNeededFlag     = true,
    paymentIsDisplayNeededFlag      = true,
    repayReallocIsDisplayNeededFlag = true
  )

  val displayNeededItemMixed: DisplayNeededItem = DisplayNeededItem(
    taxIsDisplayNeededFlag          = true,
    interestIsDisplayNeededFlag     = false,
    paymentIsDisplayNeededFlag      = true,
    repayReallocIsDisplayNeededFlag = false
  )

  def getDisplayNeeded(taxRef: Long, accPeriod: Long): DisplayNeededItem = {
    taxRef match {
      case 10L  => displayNeededItemAllFalse
      case 20L  => displayNeededItemAllTrue
      case 30L  => displayNeededItemMixed
      case 999L => throw new RuntimeException("Error from downstream")
      case _    => displayNeededItemAllFalse
    }
  }

}
