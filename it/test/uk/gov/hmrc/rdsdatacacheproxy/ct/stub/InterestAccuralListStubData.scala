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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestAccural

import java.time.LocalDate

object InterestAccuralListStubData {

  val interestAccuralListEmpty: List[InterestAccural] = List.empty

  val interestAccuralListSingleItem:  List[InterestAccural] = List(
    InterestAccural(
      computationAmount = 1,
      interestAccrualFromDate = LocalDate.of(2021, 3, 7),
      interestAccrualToDate = LocalDate.of(2021, 5, 7),
      interestRate = 2,
      interestAmount = 10,
      apEndDate = LocalDate.of(2021, 6, 7)
    )
  )

  val interestAccuralListMultipleItems: List[InterestAccural] = List(
    InterestAccural(
      computationAmount = 1,
      interestAccrualFromDate = LocalDate.of(2021, 3, 7),
      interestAccrualToDate = LocalDate.of(2021, 5, 7),
      interestRate = 2,
      interestAmount = 10,
      apEndDate = LocalDate.of(2021, 6, 7)
    ),
    InterestAccural(
      computationAmount = 1,
      interestAccrualFromDate = LocalDate.of(2021, 6, 10),
      interestAccrualToDate = LocalDate.of(2021, 8, 10),
      interestRate = 3,
      interestAmount = 23,
      apEndDate = LocalDate.of(2021, 9, 10)
    )
  )

  def getAccuralInterestListItems(taxRef: Long, accPeriod: Long, interestType: String): List[InterestAccural] = {
    taxRef match {
      case 1L  => interestAccuralListSingleItem
      case 2L  => interestAccuralListMultipleItems
      case 99L => throw new Error("Simulated downstream failure")
      case _ => interestAccuralListEmpty
    }
  }

}
