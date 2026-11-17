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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResult, JsSuccess, Json}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseMemberDetails

import java.time.LocalDate

class GroupMembersSpec extends AnyWordSpec with Matchers {

  private val validJsonGroupMembers = Json.parse(
    """
      |{
      |   "groupMembers": [
      |  {
      |   "mgdRegNumber": "XYM00000000004",
      |   "businessPartnerNumber": "0100049899",
      |   "businessName": "BusinessCo",
      |   "crn": "09876543",
      |   "foreignCorporateRef": "FCR-UK-987654",
      |   "isRepMemberOfGroup": 1,
      |   "dateOfJoining": "2026-03-31",
      |   "dateOfLeaving": "2026-06-21",
      |   "emailAddr": "a@b.com",
      |   "systemDate": "2026-10-31"
      |  },
      |  {
      |   "mgdRegNumber": "XYM00000000000",
      |   "businessPartnerNumber": "1100049911",
      |   "businessName": "Oh It's A Business",
      |   "crn": "19876544",
      |   "foreignCorporateRef": "GGR-UK-187654",
      |   "isRepMemberOfGroup": 0,
      |   "dateOfJoining": "2026-03-31",
      |   "dateOfLeaving": "2026-06-21",
      |   "emailAddr": "d@e.com",
      |   "systemDate": "2026-10-31"
      |   }
      |  ]
      |}
      |""".stripMargin
  )

  private val validJsonMemberDetails = Json.parse(
    """
      |{
      | "mgdRegNumber": "XWM00000001020",
      | "businessPartnerNumber": "0100053091",
      | "businessName": "BRUCE HOPKINS LIMITED",
      | "tradingName": "Trading name 1",
      | "crn": "324828308",
      | "dateOfIncorporation": "2025-02-15",
      | "countryOfIncorporation": "Spain",
      | "foreignCorporateRef": "FCR-UK-987654",
      | "utr": 1234567890,
      | "vrn": 127207785,
      | "tradeClass": 1,
      | "businessActivityDesc": "Business Activities",
      | "phoneNumber": "0800 123 456",
      | "mobilePhoneNumber": "07900 678 900",
      | "faxNumber": "NO MORE FAX",
      | "emailAddr": "a@b.com",
      | "adi": "I'm tired",
      | "address1": "Address 1",
      | "address2": "Address 2",
      | "address3": "Address 3",
      | "address4": "",
      | "postcode": "SA0 1AH",
      | "isIomOrCiFlag": "0",
      | "haveGamblingLicenceNo": "1",
      | "gamblingLicenceNo": "123-456789-A-123456-789",
      | "heldByLandlord": "1",
      | "localAuthority": "1",
      | "familyEntertainment": "0",
      | "clubGaming": "0",
      | "clubLicence": "1",
      | "prizeGaming": "0",
      | "onPremises": "0",
      | "clubPremises": "0",
      | "regCert": "0",
      | "bookmaking": "0",
      | "bingo": "0",
      | "amusement": "0",
      | "serveAlcohol": "0",
      | "premisesNotCovered": "0",
      | "previousMgdrn1": "XAM00000000327",
      | "previousMgdrn2": "XNM00000000457",
      | "previousMgdrn3": "XHM00000000177",
      | "associatedMgdrn1": "XYM00000000287",
      | "dateOfJoining": "2013-02-01",
      | "dateOfLeaving": "2023-03-01",
      | "isRepMemSameAsCb": "0",
      | "isUkIncorporated": "0",
      | "isRepMemberOfGroup": "1",
      | "systemDate": "2026-05-31",
      | "premises": [
      |  {
      |   "mgdRegNumber": "XCM00000001236",
      |   "address1": "1.Dalford court",
      |   "address2": "Telford",
      |   "address3": "Shropshire",
      |   "postcode": "TF3 2BP",
      |   "systemDate": "2026-06-30"
      |  },
      |  {
      |   "mgdRegNumber": "XCM00000001236",
      |   "address1": "2.Dalford court",
      |   "address2": "Telford",
      |   "address3": "Shropshire",
      |   "postcode": "TF3 2BP",
      |   "systemDate": "2026-06-30"
      |  }
      | ]
      |}
      |""".stripMargin
  )

  val joinDate: LocalDate = LocalDate.of(2026, 3, 31)
  val leaveDate: LocalDate = LocalDate.of(2026, 6, 21)
  val systemDate: LocalDate = LocalDate.of(2026, 10, 31)

  "GroupMembers JSON format" should {

    "serialize to JSON correctly" in {
      val model = GroupMembers(
        Seq(
          GroupMemberRecord(
            mgdRegNumber          = "XYM00000000004",
            businessPartnerNumber = Some("0100049899"),
            businessName          = Some("BusinessCo"),
            crn                   = Some("09876543"),
            foreignCorporateRef   = Some("FCR-UK-987654"),
            isRepMemberOfGroup    = Some(1),
            dateOfJoining         = Some(LocalDate.parse("2026-03-31")),
            dateOfLeaving         = Some(LocalDate.parse("2026-03-31")),
            emailAddr             = Some("a@b.com"),
            systemDate            = Some(LocalDate.parse("2026-10-31"))
          ),
          GroupMemberRecord(
            mgdRegNumber          = "XYM00000000004",
            businessPartnerNumber = Some("1100049911"),
            businessName          = Some("Oh It's A Business"),
            crn                   = Some("19876544"),
            foreignCorporateRef   = Some("GGR-UK-187654"),
            isRepMemberOfGroup    = Some(0),
            dateOfJoining         = Some(LocalDate.parse("2026-05-31")),
            dateOfLeaving         = Some(LocalDate.parse("2026-05-31")),
            emailAddr             = Some("d@e.com"),
            systemDate            = Some(LocalDate.parse("2026-10-31"))
          )
        )
      )

      val json = Json.toJson(model)

      (json \ "groupMembers" \ 0 \ "mgdRegNumber").as[String] mustBe "XYM00000000004"
      (json \ "groupMembers" \ 0 \ "businessPartnerNumber").as[String] mustBe "0100049899"
      (json \ "groupMembers" \ 0 \ "businessName").as[String] mustBe "BusinessCo"
      (json \ "groupMembers" \ 0 \ "crn").as[String] mustBe "09876543"
      (json \ "groupMembers" \ 0 \ "foreignCorporateRef").as[String] mustBe "FCR-UK-987654"
      (json \ "groupMembers" \ 0 \ "isRepMemberOfGroup").as[Int] mustBe 1
      (json \ "groupMembers" \ 0 \ "dateOfJoining").as[String] mustBe "2026-03-31"
      (json \ "groupMembers" \ 0 \ "dateOfLeaving").as[String] mustBe "2026-03-31"
      (json \ "groupMembers" \ 0 \ "emailAddr").as[String] mustBe "a@b.com"
      (json \ "groupMembers" \ 0 \ "systemDate").as[String] mustBe "2026-10-31"

      (json \ "groupMembers" \ 1 \ "mgdRegNumber").as[String] mustBe "XYM00000000004"
      (json \ "groupMembers" \ 1 \ "businessPartnerNumber").as[String] mustBe "1100049911"
      (json \ "groupMembers" \ 1 \ "businessName").as[String] mustBe "Oh It's A Business"
      (json \ "groupMembers" \ 1 \ "crn").as[String] mustBe "19876544"
      (json \ "groupMembers" \ 1 \ "foreignCorporateRef").as[String] mustBe "GGR-UK-187654"
      (json \ "groupMembers" \ 1 \ "isRepMemberOfGroup").as[Int] mustBe 0
      (json \ "groupMembers" \ 1 \ "dateOfJoining").as[String] mustBe "2026-05-31"
      (json \ "groupMembers" \ 1 \ "dateOfLeaving").as[String] mustBe "2026-05-31"
      (json \ "groupMembers" \ 1 \ "emailAddr").as[String] mustBe "d@e.com"
      (json \ "groupMembers" \ 1 \ "systemDate").as[String] mustBe "2026-10-31"
    }

    "deserialize from JSON correctly" in {

      val result: JsResult[GroupMembers] = validJsonGroupMembers.validate[GroupMembers]

      result mustBe JsSuccess(
        GroupMembers(
          Seq(
            GroupMemberRecord(
              "XYM00000000004",
              Some("0100049899"),
              Some("BusinessCo"),
              Some("09876543"),
              Some("FCR-UK-987654"),
              Some(1),
              Some(joinDate),
              Some(leaveDate),
              Some("a@b.com"),
              Some(systemDate)
            ),
            GroupMemberRecord(
              "XYM00000000000",
              Some("1100049911"),
              Some("Oh It's A Business"),
              Some("19876544"),
              Some("GGR-UK-187654"),
              Some(0),
              Some(joinDate),
              Some(leaveDate),
              Some("d@e.com"),
              Some(systemDate)
            )
          )
        )
      )
    }

    "round-trip (write then read) should return same object" in {
      val original = GroupMembers(Seq(GroupMemberRecord("XYZ00000000021", None, None, None, None, None, None, None, None, None)))

      val json = Json.toJson(original)
      val parsed = json.as[GroupMembers]

      parsed mustBe original
    }

    "succeed when optional fields are missing" in {
      val validJsonWithMissingFields = Json.parse(
        """
          |{
          |   "groupMembers": [
          |  {
          |   "mgdRegNumber": "XYM00000000004"
          |  }
          | ]
          |}
          |""".stripMargin
      )

      val result = validJsonWithMissingFields.validate[GroupMembers]

      result.isError mustBe false
    }

    "fail to deserialize when mandatory fields are missing" in {
      val invalidJson = Json.parse(
        """
          |{
          |   "groupMembers": [
          |  {
          |   "businessName": "asjdlkajsdlk"
          |  }
          | ]
          |}
          |""".stripMargin
      )

      val result = invalidJson.validate[GroupMembers]

      result.isError mustBe true
    }

    "fail to deserialize when types are incorrect" in {
      val json = Json.parse(
        """
          |{
          |   "groupMembers": [
          |  {
          |   "mgdRegNumber": "XYM00000000004",
          |   "crn": 2308402843208
          |  }
          | ]
          |}
          |""".stripMargin
      )

      val result = json.validate[GroupMembers]

      result.isError mustBe true
    }
  }

  "MemberDetails JSON format" should {

    "serialize to JSON correctly" in {

      val json = Json.toJson(validResponseMemberDetails)

      (json \ "mgdRegNumber").as[String] mustBe "XYM00000000001"
      (json \ "businessPartnerNumber").as[String] mustBe "0100049988"
      (json \ "businessName").as[String] mustBe "Business Name"
      (json \ "tradingName").as[String] mustBe "Trading Name"
      (json \ "crn").as[String] mustBe "09876543"
      (json \ "dateOfIncorporation").as[String] mustBe "2025-01-01"
      (json \ "countryOfIncorporation").as[String] mustBe "UK"
      (json \ "foreignCorporateRef").as[String] mustBe "FCR-UK-987654"
      (json \ "utr").as[Long] mustBe 1234567890
      (json \ "vrn").as[Long] mustBe 123456789
      (json \ "tradeClass").as[Int] mustBe 3
      (json \ "businessActivityDesc").as[String] mustBe "Business Activities"
      (json \ "phoneNumber").as[String] mustBe "0800 123 456"
      (json \ "mobilePhoneNumber").as[String] mustBe "07900 678 900"
      (json \ "faxNumber").as[String] mustBe "No one uses fax anymore"
      (json \ "emailAddr").as[String] mustBe "a@b.com"
      (json \ "adi").as[String] mustBe "our friend adi"
      (json \ "address1").as[String] mustBe "123 Road"
      (json \ "address2").as[String] mustBe "Place"
      (json \ "address3").as[String] mustBe "More info about this place"
      (json \ "address4").as[String] mustBe "Even more? noo"
      (json \ "postcode").as[String] mustBe "SA0 1AH"
      (json \ "isIomOrCiFlag").as[String] mustBe "1"
      (json \ "haveGamblingLicenceNo").as[String] mustBe "0"
      (json \ "gamblingLicenceNo").as[String] mustBe "123-456789-A-123456-789"
      (json \ "heldByLandlord").as[String] mustBe "0"
      (json \ "localAuthority").as[String] mustBe "1"
      (json \ "familyEntertainment").as[String] mustBe "0"
      (json \ "clubGaming").as[String] mustBe "1"
      (json \ "clubLicence").as[String] mustBe "0"
      (json \ "prizeGaming").as[String] mustBe "1"
      (json \ "onPremises").as[String] mustBe "0"
      (json \ "clubLicence").as[String] mustBe "0"
      (json \ "clubPremises").as[String] mustBe "1"
      (json \ "regCert").as[String] mustBe "0"
      (json \ "bookmaking").as[String] mustBe "1"
      (json \ "bingo").as[String] mustBe "0"
      (json \ "amusement").as[String] mustBe "1"
      (json \ "serveAlcohol").as[String] mustBe "0"
      (json \ "premisesNotCovered").as[String] mustBe "1"
      (json \ "previousMgdrn1").as[String] mustBe "XVM12345678800"
      (json \ "previousMgdrn2").as[String] mustBe "XVM12345678801"
      (json \ "previousMgdrn3").as[String] mustBe "XVM12345678802"
      (json \ "associatedMgdrn1").as[String] mustBe "XGM12345678800"
      (json \ "associatedMgdrn2").as[String] mustBe "XGM12345678801"
      (json \ "associatedMgdrn3").as[String] mustBe "XGM123456788002"
      (json \ "dateOfJoining").as[String] mustBe "2026-01-16"
      (json \ "dateOfLeaving").as[String] mustBe "2026-10-16"
      (json \ "isRepMemSameAsCb").as[String] mustBe "1"
      (json \ "isRepMemberOfGroup").as[String] mustBe "1"
      (json \ "isUkIncorporated").as[String] mustBe "1"
      (json \ "systemDate").as[String] mustBe "2026-11-16"
      (json \ "premises" \ 0 \ "mgdRegNumber").as[String] mustBe "XWM12345678901"
      (json \ "premises" \ 0 \ "address1").as[String] mustBe "123 Road"
      (json \ "premises" \ 0 \ "address2").as[String] mustBe "Place"
      (json \ "premises" \ 0 \ "address3").as[String] mustBe "More info about this place"
      (json \ "premises" \ 0 \ "address4").as[String] mustBe "Even more? noo"
      (json \ "premises" \ 0 \ "postcode").as[String] mustBe "SA0 1AH"
      (json \ "premises" \ 0 \ "systemDate").as[String] mustBe "2026-11-16"
      (json \ "premises" \ 1 \ "mgdRegNumber").as[String] mustBe "XWM12345678901"
      (json \ "premises" \ 1 \ "address1").as[String] mustBe "456 Road"
      (json \ "premises" \ 1 \ "address2").as[String] mustBe "Zone"
      (json \ "premises" \ 1 \ "postcode").as[String] mustBe "BA0 1HA"
      (json \ "premises" \ 1 \ "systemDate").as[String] mustBe "2026-11-16"
    }

    "deserialize from JSON correctly" in {

      val result: JsResult[MemberDetails] = validJsonMemberDetails.validate[MemberDetails]

      result mustBe JsSuccess(
        MemberDetails("XWM00000001020",
            "0100053091",
            Some("BRUCE HOPKINS LIMITED"),
            Some("Trading name 1"),
            Some("324828308"),
            Some(LocalDate.of(2025, 2, 15)),
            Some("Spain"),
            Some("FCR-UK-987654"),
            Some(1234567890),
            Some(127207785),
            Some(1),
            Some("Business Activities"),
            Some("0800 123 456"),
            Some("07900 678 900"),
            Some("NO MORE FAX"),
            Some("a@b.com"),
            Some("I'm tired"),
            Some("Address 1"),
            Some("Address 2"),
            Some("Address 3"),
            Some(""),
            Some("SA0 1AH"),
            Some("0"),
            Some("1"),
            Some("123-456789-A-123456-789"),
            Some("1"),
            Some("1"),
            Some("0"),
            Some("0"),
            Some("1"),
            Some("0"),
            Some("0"),
            Some("0"),
            Some("0"),
            Some("0"),
            Some("0"),
            Some("0"),
            Some("0"),
            Some("0"),
            Some("XAM00000000327"),
            Some("XNM00000000457"),
            Some("XHM00000000177"),
            Some("XYM00000000287"),
            None,
            None,
            Some(LocalDate.of(2013, 2, 1)),
            Some(LocalDate.of(2023, 3, 1)),
            Some("0"),
            Some("1"),
            Some("0"),
            Some(LocalDate.of(2026, 5, 31)),
            Seq(
              PremisesDetails(
                mgdRegNumber = "XCM00000001236",
                address1     = Some("1.Dalford court"),
                address2     = Some("Telford"),
                address3     = Some("Shropshire"),
                address4     = None,
                postcode     = Some("TF3 2BP"),
                systemDate   = Some(LocalDate.of(2026, 6, 30))
              ),
              PremisesDetails(
                mgdRegNumber = "XCM00000001236",
                address1     = Some("2.Dalford court"),
                address2     = Some("Telford"),
                address3     = Some("Shropshire"),
                address4     = None,
                postcode     = Some("TF3 2BP"),
                systemDate   = Some(LocalDate.of(2026, 6, 30))
              )
            )
          )
      )
    }


    "round-trip (write then read) should return same object" in {
      val original = validResponseMemberDetails

      val json = Json.toJson(original)
      val parsed = json.as[MemberDetails]

      parsed mustBe original
    }

    "succeed when optional fields are missing" in {
      val validJsonWithMissingFields = Json.parse(
        """
          |{
          |   "mgdRegNumber": "XYM00000000004",
          |   "businessPartnerNumber": "0100049899",
          |   "premises": []
          |  }
          |""".stripMargin
      )
      println(validJsonWithMissingFields)
      val result = validJsonWithMissingFields.validate[MemberDetails]

      result.isError mustBe false
    }

    "fail to deserialize when mandatory fields are missing" in {
      val invalidJson = Json.parse(
        """
          |{
          |   "businessName": "asjdlkajsdlk"
          |}
          |""".stripMargin
      )

      val result = invalidJson.validate[MemberDetails]

      result.isError mustBe true
    }

    "fail to deserialize when types are incorrect" in {
      val json = Json.parse(
        """
          |{
          |   "mgdRegNumber": "XYM00000000004",
          |   "crn": 2308402843208
          |}
          |""".stripMargin
      )

      val result = json.validate[MemberDetails]

      result.isError mustBe true
    }
  }
}
