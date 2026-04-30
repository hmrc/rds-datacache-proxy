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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class MgdCertificateSpec extends AnyWordSpec with Matchers {

  private val sampleCertificate = MgdCertificate(
    mgdRegNumber       = "XYZ00000000001",
    registrationDate   = Some(LocalDate.of(2024, 1, 1)),
    individualName     = Some("John Doe"),
    businessName       = Some("Test Business"),
    tradingName        = None,
    repMemName         = None,
    busAddrLine1       = Some("Line 1"),
    busAddrLine2       = None,
    busAddrLine3       = None,
    busAddrLine4       = None,
    busPostcode        = Some("AB1 2CD"),
    busCountry         = Some("UK"),
    busAdi             = None,
    repMemLine1        = None,
    repMemLine2        = None,
    repMemLine3        = None,
    repMemLine4        = None,
    repMemPostcode     = None,
    repMemAdi          = None,
    typeOfBusiness     = Some("Limited"),
    businessTradeClass = Some(1),
    noOfPartners       = Some(2),
    groupReg           = "N",
    noOfGroupMems      = None,
    dateCertIssued     = Some(LocalDate.of(2024, 2, 1)),
    partMembers = Seq(
      PartnerMember(
        namesOfPartMems    = Some("Partner A"),
        solePropTitle      = Some("Mr"),
        solePropFirstName  = Some("A"),
        solePropMiddleName = None,
        solePropLastName   = Some("Smith"),
        typeOfBusiness     = 1
      )
    ),
    groupMembers = Seq(
      GroupMember("Group A")
    ),
    returnPeriodEndDates = Seq(
      ReturnPeriodEndDate(LocalDate.of(2024, 3, 31))
    )
  )

  "MgdCertificate JSON format" should {

    "serialize to JSON correctly" in {
      val json = Json.toJson(sampleCertificate)

      (json \ "mgdRegNumber").as[String] mustBe "XYZ00000000001"
      (json \ "groupReg").as[String] mustBe "N"

      (json \ "partMembers").as[Seq[PartnerMember]].size mustBe 1
      (json \ "groupMembers").as[Seq[GroupMember]].size mustBe 1
    }

    "deserialize from JSON correctly" in {
      val json = Json.toJson(sampleCertificate)

      val result = json.as[MgdCertificate]

      result mustBe sampleCertificate
    }

    "handle empty optional fields" in {
      val minimal = sampleCertificate.copy(
        registrationDate = None,
        individualName   = None,
        businessName     = None
      )

      val json = Json.toJson(minimal)
      val parsed = json.as[MgdCertificate]

      parsed mustBe minimal
    }

    "format LocalDate fields as ISO strings" in {
      val json = Json.toJson(sampleCertificate)

      (json \ "registrationDate").as[String] mustBe "2024-01-01"
      (json \ "dateCertIssued").as[String] mustBe "2024-02-01"
    }

    "handle empty collections" in {
      val emptyCollections = sampleCertificate.copy(
        partMembers          = Seq.empty,
        groupMembers         = Seq.empty,
        returnPeriodEndDates = Seq.empty
      )

      val json = Json.toJson(emptyCollections)
      val parsed = json.as[MgdCertificate]

      parsed.partMembers mustBe empty
      parsed.groupMembers mustBe empty
      parsed.returnPeriodEndDates mustBe empty
    }

    "round-trip JSON should preserve the object" in {
      val json = Json.toJson(sampleCertificate)
      val parsed = json.as[MgdCertificate]

      parsed mustBe sampleCertificate
    }
  }
}
