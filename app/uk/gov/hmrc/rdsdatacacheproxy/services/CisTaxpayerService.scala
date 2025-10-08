/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.services

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.models.CisTaxpayer
import uk.gov.hmrc.rdsdatacacheproxy.repositories.CisMonthlyReturnSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisTaxpayerService @Inject() (cisSource: CisMonthlyReturnSource)(implicit ec: ExecutionContext) extends Logging {

  def getCisTaxpayerByTaxReference(
    taxOfficeNumber: String,
    taxOfficeReference: String
  ): Future[CisTaxpayer] = {
    cisSource.getCisTaxpayerByTaxRef(taxOfficeNumber, taxOfficeReference).map {
      case Some(t) if t.uniqueId.trim.nonEmpty => t
      case Some(_) =>
        val msg = s"[CIS] Contractor found but UNIQUE_ID missing/blank for TON=$taxOfficeNumber, TOR=$taxOfficeReference"
        logger.warn(msg)
        throw new NoSuchElementException(msg)
      case None =>
        val msg = s"[CIS] No contractor found for TON=$taxOfficeNumber, TOR=$taxOfficeReference"
        logger.warn(msg)
        throw new NoSuchElementException(msg)
    }
  }
}
