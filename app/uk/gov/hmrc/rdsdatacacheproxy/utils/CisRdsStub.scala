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

package uk.gov.hmrc.rdsdatacacheproxy.utils

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.models.CisTaxpayer
import uk.gov.hmrc.rdsdatacacheproxy.repositories.CisMonthlyReturnSource

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CisRdsStub @Inject()() extends CisMonthlyReturnSource with Logging {

  override def getCisTaxpayerByTaxRef(
                                       taxOfficeNumber: String,
                                       taxOfficeReference: String
                                     ): Future[Option[CisTaxpayer]] = {
    val tonOk = Option(taxOfficeNumber).exists(_.trim.nonEmpty)
    val torOk = Option(taxOfficeReference).exists(_.trim.nonEmpty)

    if (tonOk && torOk) {
      val ton = taxOfficeNumber.trim
      val tor = taxOfficeReference.trim

      val taxpayer = CisTaxpayer(
        uniqueId = "1",
        taxOfficeNumber = ton,
        taxOfficeRef = tor,
        aoDistrict = Some("123"),
        aoPayType = Some("M"),
        aoCheckCode = Some("XY"),
        aoReference = Some("1234567XY"),
        validBusinessAddr = Some("Y"),
        correlation = Some("corr-abc"),
        ggAgentId = Some("AGENT-001"),
        employerName1 = Some("TEST LTD"),
        employerName2 = None,
        agentOwnRef = Some("AG-REF-001"),
        schemeName = Some("CIS Scheme"),
        utr = Some("1234567890"),
        enrolledSig = Some("Y")
      )

      logger.info(s"[CIS-STUB] getCisTaxpayerByTaxRef -> TON=$ton, TOR=$tor => uniqueId=${taxpayer.uniqueId}")
      Future.successful(Some(taxpayer))
    } else {
      logger.warn(s"[CIS-STUB] getCisTaxpayerByTaxRef -> missing/blank TON/TOR: ton='$taxOfficeNumber', tor='$taxOfficeReference'")
      Future.successful(None)
    }
  }
}
