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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{PayRepayReallocationsList, PayRepayReallocations}


object PayRepayReallocationStubData {

  val payRepayReallocationsList: PayRepayReallocationsList = PayRepayReallocationsList(
    List(
      PayRepayReallocations(
        totalAmountReoRfrRto = Some(BigDecimal(10)),
        totalAmountPayments = Some(BigDecimal(20))
      ),
      PayRepayReallocations(
        totalAmountReoRfrRto = Some(BigDecimal(30)),
        totalAmountPayments = Some(BigDecimal(40))
      ),
      PayRepayReallocations(
        totalAmountReoRfrRto = Some(BigDecimal(50)),
        totalAmountPayments = Some(BigDecimal(60))
      )
    )
  )

  val emptyPayRepayReallocationsList: PayRepayReallocationsList = PayRepayReallocationsList(List.empty)

  def getTotalAmounts(taxRef: Long, accPeriod: Long): PayRepayReallocationsList = {
    taxRef match {
      case 10L  => payRepayReallocationsList
      case 200L => throw new RuntimeException("Downstream error")
      case _    => emptyPayRepayReallocationsList
    }
  }

}
