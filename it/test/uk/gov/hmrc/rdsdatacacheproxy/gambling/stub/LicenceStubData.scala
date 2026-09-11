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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.LicenceDetails

import java.time.LocalDate

object LicenceStubData {

  def getLicenceDetailsData(regNumber: String): LicenceDetails =
    regNumber match {
      case "XEM00000001335" =>
        LicenceDetails(
          mgdRegNumber          = regNumber,
          haveGamblingLicenceNo = Some("1"),
          gamblingLicenceNo     = "123-456789-A-123456-789",
          heldByLandlord        = Some("1"),
          localAuthority        = Some("1"),
          familyEntertainment   = Some("0"),
          clubGaming            = Some("0"),
          clubLicence           = Some("1"),
          prizeGaming           = Some("0"),
          onPremises            = Some("1"),
          clubPremises          = Some("0"),
          regCert               = Some("0"),
          bookmaking            = Some("0"),
          bingo                 = Some("0"),
          amusement             = Some("0"),
          serveAlcohol          = Some("0"),
          premisesNotCovered    = Some("0"),
          systemDate            = Some(LocalDate.of(2026, 5, 31))
        )
      case "XEM00000000000" =>
        LicenceDetails(
          mgdRegNumber          = regNumber,
          haveGamblingLicenceNo = Some("0"),
          gamblingLicenceNo     = "",
          heldByLandlord        = Some("0"),
          localAuthority        = Some("0"),
          familyEntertainment   = Some("0"),
          clubGaming            = Some("0"),
          clubLicence           = Some("0"),
          prizeGaming           = Some("0"),
          onPremises            = Some("0"),
          clubPremises          = Some("0"),
          regCert               = Some("0"),
          bookmaking            = Some("0"),
          bingo                 = Some("0"),
          amusement             = Some("0"),
          serveAlcohol          = Some("0"),
          premisesNotCovered    = Some("0"),
          systemDate            = Some(LocalDate.of(2026, 5, 31))
        )
      case "XZM33333066666" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        LicenceDetails(
          mgdRegNumber          = regNumber,
          haveGamblingLicenceNo = Some("0"),
          gamblingLicenceNo     = "",
          heldByLandlord        = Some("0"),
          localAuthority        = Some("0"),
          familyEntertainment   = Some("0"),
          clubGaming            = Some("0"),
          clubLicence           = Some("0"),
          prizeGaming           = Some("0"),
          onPremises            = Some("0"),
          clubPremises          = Some("0"),
          regCert               = Some("0"),
          bookmaking            = Some("0"),
          bingo                 = Some("0"),
          amusement             = Some("0"),
          serveAlcohol          = Some("0"),
          premisesNotCovered    = Some("0"),
          systemDate            = Some(LocalDate.of(2026, 5, 31))
        )
    }
}
