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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.stub

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.SubmittedReturnSingle

import java.time.LocalDate

object SubmittedReturnSingleStubData {

  def getSubmittedReturnSingleData(regNumber: String, consecNo: Int): SubmittedReturnSingle =
    regNumber match {
      case "XYZ00000000001" =>
        SubmittedReturnSingle(
          consecNo                     = 23,
          mgdPeriod                    = "01/01/2025 - 30/03/2025",
          submittedDate                = LocalDate.of(2025, 5, 1),
          ackRef                       = "123456789012345",
          noOfMachines                 = 5,
          netTakingsHigherRate         = 100.10,
          netTakingsStdRate            = 20.00,
          netTakingsLowerRate          = 200.20,
          totalDueHigherRate           = 10.00,
          totalDueStdRate              = 300.30,
          totalDueLowerRate            = 5.00,
          dutyPayable                  = 35.00,
          underDeclaredDuty            = 40.00,
          previousReturnAmount         = 100.00,
          negativeAmountCarriedForward = 99.99,
          totalNetDutyPayable          = 75.49
        )
      case "XVM33333333333" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        SubmittedReturnSingle(
          consecNo                     = 123,
          mgdPeriod                    = "01/01/2015 - 30/03/2015",
          submittedDate                = LocalDate.of(2015, 5, 1),
          ackRef                       = "ABCDEFGHIJKLMN",
          noOfMachines                 = 50,
          netTakingsHigherRate         = 1000.10,
          netTakingsStdRate            = 200.00,
          netTakingsLowerRate          = 2000.20,
          totalDueHigherRate           = 100.00,
          totalDueStdRate              = 3000.30,
          totalDueLowerRate            = 50.00,
          dutyPayable                  = 350.00,
          underDeclaredDuty            = 400.00,
          previousReturnAmount         = 1000.00,
          negativeAmountCarriedForward = 990.99,
          totalNetDutyPayable          = 750.49
        )
    }
}
