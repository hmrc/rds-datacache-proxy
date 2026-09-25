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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{GpaPaymentsDetails, GpaPaymentsItem, GroupReferenceNumberLstItem, GroupSummaryDetails, GroupSummaryDetailsItem}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.GroupPaymentsRepository

import java.time.LocalDate
import scala.concurrent.Future

trait GroupPaymentsHelper {

  val groupSummaryDetItemOne = GroupSummaryDetailsItem(
    contractEndDate         = LocalDate.of(2026, 1, 7),
    groupTaxCharge          = Some(BigDecimal(11.01)),
    groupPayment            = Some(BigDecimal(13.02)),
    groupPaymentRecordCount = 2,
    contractStatus          = "ACTIVE",
    contractVersion         = 1
  )

  val groupPaymentDetails = GroupSummaryDetails(
    gpaGrpSummaryDetails = List(
      groupSummaryDetItemOne
    ),
    gpaReferenceNumberLst = List(
      GroupReferenceNumberLstItem(112)
    ),
    nominatedCompanyName = "Some company name"
  )

  val groupPaymentDetailsEmpty = GroupSummaryDetails(
    gpaGrpSummaryDetails  = List.empty,
    gpaReferenceNumberLst = List.empty,
    nominatedCompanyName  = "CompanyName"
  )

  val defaultPaymentDetails = GpaPaymentsDetails(
    gpaPayments = List(
      GpaPaymentsItem(
        displayDate             = Some(LocalDate.of(2026, 2, 3)),
        total                   = Some(BigDecimal(6.71)),
        tablename               = Some("Payslip"),
        targetTaxpayerReference = Some("005"),
        targetApNo              = Some(5),
        targetApEndDate         = Some(LocalDate.of(2025, 7, 3)),
        contractEndDate         = Some(LocalDate.of(2026, 8, 3)),
        participatorPresent     = Some(true)
      )
    ),
    totalNumOfRecords      = Some(5L),
    gppEndDate             = Some(LocalDate.of(2026, 2, 3)),
    gppTotalGroupPayment   = Some(BigDecimal(346.71)),
    gppTotalGroupTax       = Some(BigDecimal(16.21)),
    gppStatus              = "ACTIVE",
    gppCni                 = Some(LocalDate.of(2026, 5, 3)),
    gppApportionmentMethod = "METHOD"
  )

  val emptyPaymentDetailsRec = GpaPaymentsDetails(
    gpaPayments            = List.empty,
    totalNumOfRecords      = None,
    gppEndDate             = None,
    gppTotalGroupPayment   = None,
    gppTotalGroupTax       = None,
    gppStatus              = "ACTIVE",
    gppCni                 = None,
    gppApportionmentMethod = "METHOD"
  )

  class GroupPaymentsRepositoryDataSource extends GroupPaymentsRepository {

    override def getGroupSummary(gpaUTR: Long, nomCompanyUTR: Long): Future[Option[GroupSummaryDetails]] = {
      gpaUTR match {
        case 1 =>
          Future.successful(Some(groupPaymentDetails))
        case 5 =>
          Future.successful(None)
        case 99 =>
          Future.successful(throw new Error("Boom"))
        case _ =>
          Future.successful(
            Some(
              GroupSummaryDetails(
                gpaGrpSummaryDetails  = List.empty,
                gpaReferenceNumberLst = List.empty,
                nominatedCompanyName  = "Empty company name"
              )
            )
          )
      }
    }

    override def getPaymentsDetails(gpaUTR: Long, contractVersion: Int, startIndex: Int, count: Int): Future[Option[GpaPaymentsDetails]] = {
      gpaUTR match {
        case 1 => // default record
          Future.successful(Some(defaultPaymentDetails))
        case 99 =>
          Future.successful(throw new Error("Boom"))
        case 11 => // record with empty fields
          Future.successful(
            Some(
              emptyPaymentDetailsRec
            )
          )
        case _ =>
          Future.successful(None)
      }
    }
  }

}
