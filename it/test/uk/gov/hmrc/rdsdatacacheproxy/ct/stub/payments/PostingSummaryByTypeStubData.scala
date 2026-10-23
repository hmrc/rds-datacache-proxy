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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentAllocationChequeDetails, PaymentAllocationChequeRow, PostingSummaryByTypeResponse, PostingSummaryByTypeRow}

trait PostingSummaryByTypeStubData {

  val emptyPostingSummaryByTypeResponses: PostingSummaryByTypeResponse = PostingSummaryByTypeResponse(postingSummaryByTypeResponse = List.empty)

  val postingSummaryByTypeRowMultiple: PostingSummaryByTypeResponse =
    PostingSummaryByTypeResponse(
      List(
        PostingSummaryByTypeRow(
          accountingPeriod       = Some(1),
          settlementSequenceNo = Some(1),
          postingAmountSum = Some(123456)
        ),
        PostingSummaryByTypeRow(
          accountingPeriod       = Some(2),
          settlementSequenceNo = Some(2),
          postingAmountSum = Some(456789)
        ),
        PostingSummaryByTypeRow(
          accountingPeriod       = Some(3),
          settlementSequenceNo = Some(3),
          postingAmountSum = Some(159159)
        )
      )
    )

  val postingSummaryByTypeRowMultipleMissingFields: PostingSummaryByTypeResponse =
    PostingSummaryByTypeResponse(
      List(
        PostingSummaryByTypeRow(
          accountingPeriod       = Some(1),
          settlementSequenceNo = Some(1),
          postingAmountSum = None
        ),
        PostingSummaryByTypeRow(
          accountingPeriod       = Some(2),
          settlementSequenceNo = None,
          postingAmountSum = Some(456789)
        ),
        PostingSummaryByTypeRow(
          accountingPeriod       = None,
          settlementSequenceNo = Some(3),
          postingAmountSum = Some(159159)
        )
      )
    )

  def getPostingSummaryByPostingTypeData(taxRef: Long, postingType: String): PostingSummaryByTypeResponse = {
    (taxRef, postingType) match {
      case (1L, "PAF")  => postingSummaryByTypeRowMultiple
      case (2L, "PYT")  => postingSummaryByTypeRowMultipleMissingFields
      case (3L, "SAM")  => throw new RuntimeException("Error from downstream")
      case (_, _)       => emptyPostingSummaryByTypeResponses
    }
  }

}
