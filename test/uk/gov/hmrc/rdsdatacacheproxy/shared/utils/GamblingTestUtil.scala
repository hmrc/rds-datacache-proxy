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

package uk.gov.hmrc.rdsdatacacheproxy.shared.utils

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{AmountDeclared, ReturnsSubmitted}

import java.time.LocalDate

object GamblingTestUtil {

  final val validRegime = "GbD"

  val validResponseReturnsSubmitted = ReturnsSubmitted(
    periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
    periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
    total              = Some(-24500.00),
    totalPeriodRecords = Some(3),
    amountDeclared = Seq(
      AmountDeclared(descriptionCode = Some(2650),
                     periodStartDate = Some(LocalDate.of(2014, 4, 1)),
                     periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
                     amount          = Some(-9500.00)
                    ),
      AmountDeclared(descriptionCode = Some(2650),
                     periodStartDate = Some(LocalDate.of(2014, 1, 1)),
                     periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
                     amount          = Some(-8000.00)
                    ),
      AmountDeclared(descriptionCode = Some(2650),
                     periodStartDate = Some(LocalDate.of(2013, 10, 1)),
                     periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
                     amount          = Some(-7000.00)
                    )
    )
  )
}
