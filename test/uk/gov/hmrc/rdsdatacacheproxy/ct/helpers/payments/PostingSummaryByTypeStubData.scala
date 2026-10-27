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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PostingSummaryByTypeResponse, PostingSummaryByTypeRow}

trait PostingSummaryByTypeStubData {

  val emptyPostingSummaryByTypeResponses: PostingSummaryByTypeResponse = PostingSummaryByTypeResponse(List.empty)

  val postingSummaryByTypeRowMultiple: PostingSummaryByTypeResponse = PostingSummaryByTypeResponse(
    List(
      PostingSummaryByTypeRow(
        accountingPeriod     = 1,
        settlementSequenceNo = Some(1),
        postingAmountSum     = 123456
      ),
      PostingSummaryByTypeRow(
        accountingPeriod     = 2,
        settlementSequenceNo = Some(2),
        postingAmountSum     = 456789
      ),
      PostingSummaryByTypeRow(
        accountingPeriod     = 3,
        settlementSequenceNo = Some(3),
        postingAmountSum     = 159159
      )
    )
  )

  val postingSummaryByTypeRowMissingField: PostingSummaryByTypeResponse =
    PostingSummaryByTypeResponse(
      List(
        PostingSummaryByTypeRow(
          accountingPeriod     = 1,
          settlementSequenceNo = None,
          postingAmountSum     = 1
        )
      )
    )

  def getPostingSummaryByTypeSummary(taxRef: Long, postingType: String): PostingSummaryByTypeResponse = {
    (taxRef, postingType) match {
      case (1L, "PAF") => postingSummaryByTypeRowMultiple
      case (2L, "PYT") => postingSummaryByTypeRowMissingField
      case (3L, "SAM") => emptyPostingSummaryByTypeResponses
      case (_, _)      => throw new RuntimeException("Error from downstream")
    }
  }
}
