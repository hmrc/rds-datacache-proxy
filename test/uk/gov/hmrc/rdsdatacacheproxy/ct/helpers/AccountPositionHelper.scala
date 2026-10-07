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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{AccountPositionResponse, ApAmountItem}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AccountPositionRepository

import java.time.LocalDate
import scala.concurrent.Future

trait AccountPositionHelper {

  val apItemOne = ApAmountItem(
    accountingPeriod = 51L,
    apEndDate        = Some(LocalDate.of(2024, 2, 3)),
    amountDueForAp   = Some(BigDecimal(4.326)),
    apStatus         = Some("N")
  )

  val defaultRecord = AccountPositionResponse(
    amountDue    = Some(BigDecimal(15.18)),
    asOnDate     = Some(LocalDate.of(2026, 1, 1)),
    gpaLinkFlag  = Some("N"),
    taxpayerList = List("1002"),
    apAmounts = List(
      apItemOne
    ),
    doesCompanyExist = Some("Y")
  )

  val emptyRecord = AccountPositionResponse(
    amountDue        = None,
    asOnDate         = None,
    gpaLinkFlag      = None,
    taxpayerList     = List.empty,
    apAmounts        = List.empty,
    doesCompanyExist = None
  )

  class AccountPositionRepositoryRdsStub extends AccountPositionRepository {

    def getAccountPosition(taxRef: Long): Future[Option[AccountPositionResponse]] = {
      taxRef match {
        case 1L =>
          Future.successful {
            Some(defaultRecord)
          }
        case 11L =>
          Future.successful(throw new Error("Simulated downstream failure"))
        case 31L =>
          Future.successful(None)
        case _ =>
          Future.successful {
            Some(emptyRecord)
          }
      }
    }

  }

}
