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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Partner, PartnerDetails}

import java.time.LocalDate

object PartnerDetailsStubData {
  def getPartnerDetailsData(regNumber: String): PartnerDetails =
    regNumber match {
      case "XYZ00000000000" =>
        PartnerDetails(
          partners = List(
            Partner(
              mgdRegNumber           = "XYZ00000000000",
              businessPartnerNumber         = Some("0100049899"),
              dateOfJoining          = Some(LocalDate.of(2025, 1, 1)),
              dateOfLeaving          = Some(LocalDate.of(2026, 1, 1)),
              solePropTitle          = Some("Ms"),
              solePropFirstName      = Some("Amelia"),
              solePropMiddleName     = Some("Rose"),
              solePropLastName       = Some("Hartley"),
              businessName           = Some("Hartley Financial Services"),
              tradingName            = Some("Hartley Advisory"),
              dateOfBirth            = Some(LocalDate.of(1986, 9, 22)),
              nino                   = Some("QQ123456C"),
              utr                    = Some("1234567890"),
              vrn                    = Some("GB123456789"),
              crn                    = Some("09876543"),
              dateOfIncorporation    = Some(LocalDate.of(2022, 11, 1)),
              countryOfIncorporation = Some("United Kingdom"),
              foreignCorporateRef    = Some("FCR-UK-987654"),
              address1               = Some("42 Mockingbird Lane"),
              address2               = Some("Suite 5"),
              address3               = Some("Westbridge Business Park"),
              address4               = Some("Bristol"),
              postcode               = Some("BS1 4AB"),
              country                = Some("United Kingdom"),
              adi                    = Some("ADI-123456"),
              iomOrCiFlag            = Some("N"),
              phoneNumber            = Some("0117 555 1234"),
              mobilePhoneNumber      = Some("07700 900123"),
              faxNumber              = Some("0117 555 5678"),
              emailAddr              = Some("amelia.hartley@example.test"),
              isFutureLeaveDate      = Some(1),
              isFutureJoinDate       = Some(0),
              businessType           = Some(2)
            )
          ),
          systemDate = Some(LocalDate.of(2026, 7, 30))
        )
      case "XYZ00000000001" =>
        PartnerDetails(
          partners = List(
            Partner(
              mgdRegNumber           = "XYZ00000000001",
              businessPartnerNumber         = Some("0100049899"),
              dateOfJoining          = Some(LocalDate.of(2025, 1, 1)),
              dateOfLeaving          = Some(LocalDate.of(2026, 1, 1)),
              solePropTitle          = Some("Mr"),
              solePropFirstName      = Some("Tom"),
              solePropMiddleName     = Some("Jack"),
              solePropLastName       = Some("Hartley"),
              businessName           = Some("Hartley Financial Services"),
              tradingName            = Some("Hartley Advisory"),
              dateOfBirth            = Some(LocalDate.of(1986, 9, 22)),
              nino                   = Some("QQ123456C"),
              utr                    = Some("1234567890"),
              vrn                    = Some("GB123456789"),
              crn                    = Some("09876543"),
              dateOfIncorporation    = Some(LocalDate.of(2022, 11, 1)),
              countryOfIncorporation = Some("United Kingdom"),
              foreignCorporateRef    = Some("FCR-UK-987654"),
              address1               = Some("42 Mockingbird Lane"),
              address2               = Some("Suite 5"),
              address3               = Some("Westbridge Business Park"),
              address4               = Some("Bristol"),
              postcode               = Some("BS1 4AB"),
              country                = Some("United Kingdom"),
              adi                    = Some("ADI-123456"),
              iomOrCiFlag            = Some("N"),
              phoneNumber            = Some("0117 555 1234"),
              mobilePhoneNumber      = Some("07700 900123"),
              faxNumber              = Some("0117 555 5678"),
              emailAddr              = Some("amelia.hartley@example.test"),
              isFutureLeaveDate      = Some(1),
              isFutureJoinDate       = Some(0),
              businessType           = Some(2)
            )
          ),
          systemDate = Some(LocalDate.of(2026, 7, 30))
        )
      case _ => throw new RuntimeException("Simulated downstream failure")
    }
}
