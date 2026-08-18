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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{PenaltyTransaction, TaxTransactionsItem, ReallocationRow, PaymentTransactions}

import java.time.LocalDate

object CorporationTaxStubData {

  // Penalties
  val penaltiesEmptyList: List[PenaltyTransaction] = List.empty

  val penaltiesItems: List[PenaltyTransaction] = List(
    PenaltyTransaction(penaltyDate = LocalDate.of(2025, 5, 1), `type` = "F", postingAmount = BigDecimal(100.13)),
    PenaltyTransaction(penaltyDate = LocalDate.of(2021, 3, 7), `type` = "G", postingAmount = BigDecimal(27.19))
  )

  def getPenaltiesItems(taxRef: Long): List[PenaltyTransaction] = {
    taxRef match {
      case 1L => penaltiesItems
      case 19L => throw new Error("Simulated downstream failure")
      case _ => penaltiesEmptyList
    }
  }

  def getTaxTransactions(taxRef: Long, accPeriod: Long): List[TaxTransactionsItem] =
    (taxRef, accPeriod) match {
      case (1, _) => List(
        TaxTransactionsItem(currentAmount = 123.44, assessmentType = "A", taxDate = LocalDate.of(2026, 1, 1), correctionClaimSignal = Some("1")),
        TaxTransactionsItem(currentAmount = 1234.94, assessmentType = "D", taxDate = LocalDate.of(2026, 2, 1), correctionClaimSignal = Some("1")),
        TaxTransactionsItem(currentAmount = 463.23, assessmentType = "E", taxDate = LocalDate.of(2026, 3, 1), correctionClaimSignal = Some("1"))
      )
      case (2, _) => List.empty
      case (99, _) => throw new Error("Downstream error")
    }

  // Reallocations
  val reallocationsEmpty: Seq[ReallocationRow] = Seq[ReallocationRow]()
  val reallocationsSingleItem: Seq[ReallocationRow] = Seq[ReallocationRow](
    ReallocationRow(
      amount = BigDecimal(117.01),
      reallocationDate = LocalDate.of(2025, 5, 1),
      sourceApEndDate = Some(LocalDate.of(2026, 7, 1)),
      sourceTaxpayerReference = "9369369363"
    )
  )

  val reallocationsTwoItems: Seq[ReallocationRow] = Seq[ReallocationRow](
    ReallocationRow(
      amount = BigDecimal(117.01),
      reallocationDate = LocalDate.of(2025, 5, 1),
      sourceApEndDate = Some(LocalDate.of(2026, 7, 1)),
      sourceTaxpayerReference = "9369369363"
    ),
    ReallocationRow(
      amount = BigDecimal(27.89),
      reallocationDate = LocalDate.of(2015, 1, 1),
      sourceApEndDate = Some(LocalDate.of(2025, 11, 8)),
      sourceTaxpayerReference = "9369369361"
    )
  )

  def getReallocations(taxRef: Long, accPeriod: Long): Seq[ReallocationRow] =
    (taxRef, accPeriod) match {
      case (0, _) =>
        reallocationsEmpty
      case (1, _) => reallocationsSingleItem
      case (2, _) => reallocationsTwoItems
      case (_, _) => throw new Error("Simulated downstream failure")
    }

  // Payments
  def getPayments(taxRef: Long, accPeriod: Long): List[PaymentTransactions] =
    (taxRef, accPeriod) match {
      case (1, _) => List(
        PaymentTransactions(amount = 123.44, paymentType = "CP", effectiveDateOfPayment = LocalDate.of(2026, 1, 1)),
        PaymentTransactions(amount = 3213.44, paymentType = "CP", effectiveDateOfPayment = LocalDate.of(2026, 2, 1)),
        PaymentTransactions(amount = 56785.45, paymentType = "CP", effectiveDateOfPayment = LocalDate.of(2026, 1, 23)),
      )
      case (2, _) => List.empty
      case (99, _) => throw new Error("Downstream error")
    }

}
