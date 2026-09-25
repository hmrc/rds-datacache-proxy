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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.{GpaGroupTaxCharges, ParticipatorDetails}

import java.time.LocalDate

trait GroupTaxChargesStubData {

  val gpaWithEmptyParticipator: GpaGroupTaxCharges = GpaGroupTaxCharges(
    pGppEndDate              = Some(LocalDate.of(2023, 4, 5)),
    pGppTotalGroupPayment    = Some(BigDecimal(0)),
    pGppTotalGroupTax        = Some(BigDecimal(0)),
    pGppStatus               = Some("PENDING"),
    pGppCni                  = Some(LocalDate.of(2024, 3, 19)),
    pGppApportionmentMethod  = Some("NOT-EQUAL"),
    pGpaUtr2                 = 1000L,
    pTotalNumOfRecords       = Some(0),
    pGroupPaymentRecordCount = Some(0),
    pCurGroupTaxCharges      = List.empty
  )
  val gpaWithEmptyGppApportionmentMethod: GpaGroupTaxCharges = GpaGroupTaxCharges(
    pGppEndDate              = Some(LocalDate.of(2023, 4, 5)),
    pGppTotalGroupPayment    = Some(BigDecimal(0)),
    pGppTotalGroupTax        = Some(BigDecimal(0)),
    pGppStatus               = Some("PENDING"),
    pGppCni                  = Some(LocalDate.of(2024, 3, 19)),
    pGppApportionmentMethod  = Some("                                   "),
    pGpaUtr2                 = 1000L,
    pTotalNumOfRecords       = Some(0),
    pGroupPaymentRecordCount = Some(0),
    pCurGroupTaxCharges      = List.empty
  )
  val gpaWithFilteredGppApportionmentMethod: GpaGroupTaxCharges = GpaGroupTaxCharges(
    pGppEndDate              = Some(LocalDate.of(2023, 4, 5)),
    pGppTotalGroupPayment    = Some(BigDecimal(0)),
    pGppTotalGroupTax        = Some(BigDecimal(0)),
    pGppStatus               = Some("PENDING"),
    pGppCni                  = Some(LocalDate.of(2024, 3, 19)),
    pGppApportionmentMethod  = Some(""),
    pGpaUtr2                 = 1000L,
    pTotalNumOfRecords       = Some(0),
    pGroupPaymentRecordCount = Some(0),
    pCurGroupTaxCharges      = List.empty
  )

  val gpaWithNonEmptyParticipator: GpaGroupTaxCharges = GpaGroupTaxCharges(
    pGppEndDate              = Some(LocalDate.of(2023, 4, 5)),
    pGppTotalGroupPayment    = Some(BigDecimal(15000.50)),
    pGppTotalGroupTax        = Some(BigDecimal(3200.75)),
    pGppStatus               = Some("SUBMITTED"),
    pGppCni                  = Some(LocalDate.of(2023, 3, 1)),
    pGppApportionmentMethod  = Some("EQUAL"),
    pGpaUtr2                 = 200L,
    pTotalNumOfRecords       = Some(3),
    pGroupPaymentRecordCount = Some(3),
    pCurGroupTaxCharges = List(
      ParticipatorDetails(
        participatorName             = "Company A Ltd",
        participatorReference        = 1234567890L,
        participatorApEndDate        = LocalDate.of(2023, 3, 31),
        participatorTaxCharge        = BigDecimal(1066.92),
        participatorTaxChargePrsnt   = "Y",
        participatorAccountingPeriod = 1L,
        contractVersion              = 1L,
        allocatedPayment             = BigDecimal(5000.00),
        allocatedPaymentRecordCount  = 1
      ),
      ParticipatorDetails(
        participatorName             = "Company B Ltd",
        participatorReference        = 2345678901L,
        participatorApEndDate        = LocalDate.of(2023, 3, 31),
        participatorTaxCharge        = BigDecimal(1280.11),
        participatorTaxChargePrsnt   = "Y",
        participatorAccountingPeriod = 1L,
        contractVersion              = 1L,
        allocatedPayment             = BigDecimal(6000.50),
        allocatedPaymentRecordCount  = 1
      )
    )
  )

  def getGroupTaxCharges(pGpaUtr: Long, pGppContractVersion: Long, pStartIndex: Long, pCount: Long): GpaGroupTaxCharges = {
    (pGpaUtr, pGppContractVersion, pStartIndex, pCount) match {
      case (1, 2, 3, 4)     => gpaWithEmptyParticipator
      case (12, 13, 14, 15) => gpaWithNonEmptyParticipator
      case _                => throw Error("No Data found")
    }
  }
}
