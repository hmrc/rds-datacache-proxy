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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{SubmittedReturns, SubmittedReturnsItem}

import java.time.LocalDate

object SubmittedReturnsStubData {
  val DEFAULT_SORT_BY = 3
  val DEFAULT_ORDER_BY = "ASC"

  def getSubmittedReturnsData(regNumber: String, sortBy: Int, orderBy: String): SubmittedReturns =
    regNumber match {
      case "XYZ00000000000" =>
        SubmittedReturns(
          items = Seq()
        )
      case "XYZ00000000001" =>
        SubmittedReturns(
          items = Seq(
            SubmittedReturnsItem(
              consec_no      = 12345,
              mgd_period     = "01/01/2025 - 30/03/2025",
              submitted_date = LocalDate.of(2025, 4, 1),
              ack_ref        = s"$sortBy $orderBy"
            ),
            SubmittedReturnsItem(
              consec_no      = 22345,
              mgd_period     = "01/04/2025 - 30/06/2025",
              submitted_date = LocalDate.of(2025, 7, 1),
              ack_ref        = s"$sortBy $orderBy"
            ),
            SubmittedReturnsItem(
              consec_no      = 111222,
              mgd_period     = "10/02/2024 - 29/04/2024",
              submitted_date = LocalDate.of(2024, 5, 1),
              ack_ref        = s"$sortBy $orderBy"
            )
          )
        )
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        SubmittedReturns(
          items = Seq(
            SubmittedReturnsItem(
              consec_no      = 55555,
              mgd_period     = "01/01/2025 - 30/03/2025",
              submitted_date = LocalDate.of(2025, 4, 1),
              ack_ref        = s"$sortBy $orderBy"
            ),
            SubmittedReturnsItem(
              consec_no      = 66666,
              mgd_period     = "01/04/2025 - 30/06/2025",
              submitted_date = LocalDate.of(2025, 7, 1),
              ack_ref        = s"$sortBy $orderBy"
            ),
            SubmittedReturnsItem(
              consec_no      = 77777,
              mgd_period     = "10/02/2024 - 29/04/2024",
              submitted_date = LocalDate.of(2024, 5, 1),
              ack_ref        = s"$sortBy $orderBy"
            )
          )
        )
    }

}
