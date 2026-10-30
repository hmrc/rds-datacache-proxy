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

import java.time.LocalDate

class GroupMembersSpec extends AnyWordSpec with Matchers {

  private val validJson = Json.parse(
    """
      |{
      |   "groupMemberRecords": [
      |  {
      |   "mgdRegNumber": "XYM00000000004",
      |   "businessPartnerNumber": "0100049899",
      |   "businessName": "BusinessCo",
      |   "crn": "09876543",
      |   "foreignCorporateRef": "FCR-UK-987654",
      |   "isRepMemberOfGroup": 1,
      |   "dateOfJoining": "2026-03-31",
      |   "dateOfLeaving": "2026-06-21",
      |   "emailAddress": "a@b.com"
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
      |   "emailAddress": "d@e.com"
      |   }
      |  ],
      |  "systemDate": "2026-01-01"
      |}
      |""".stripMargin
  )



  "GroupMembers JSON format" should {

    "serialize to JSON correctly" in {
      val model = GroupMembers(
        Seq(GroupMemberRecord(
          mgdRegNumber = "XYM00000000004",
          businessPartnerNumber = Some("0100049899"),
          businessName = Some("BusinessCo"),
          crn = Some("09876543"),
          foreignCorporateRef = Some("FCR-UK-987654"),
          isRepMemberOfGroup = Some(1),
          dateOfJoining = Some(LocalDate.parse("2026-03-31")),
          dateOfLeaving = Some(LocalDate.parse("2026-03-31")),
          emailAddress = Some("a@b.com")
        ),
        GroupMemberRecord(
          mgdRegNumber = "XYM00000000000",
          businessPartnerNumber = Some("1100049911"),
          businessName = Some("Oh It's A Business"),
          crn = Some("19876544"),
          foreignCorporateRef = Some("GGR-UK-187654"),
          isRepMemberOfGroup = Some(0),
          dateOfJoining = Some(LocalDate.parse("2026-05-31")),
          dateOfLeaving = Some(LocalDate.parse("2026-05-31")),
          emailAddress = Some("d@e.com")
        )),
        systemDate = Some(LocalDate.of(2026, 10, 29))
      )

      val json = Json.toJson(model)

      (json \ "groupMemberRecords" \ 0 \ "mgdRegNumber").as[String] mustBe "XYM00000000004"
      (json \ "groupMemberRecords" \ 0 \ "businessPartnerNumber").as[String] mustBe "0100049899"
      (json \ "groupMemberRecords" \ 0 \ "businessName").as[String] mustBe "BusinessCo"
      (json \ "groupMemberRecords" \ 0 \ "crn").as[String] mustBe "09876543"
      (json \ "groupMemberRecords" \ 0 \ "foreignCorporateRef").as[String] mustBe "FCR-UK-987654"
      (json \ "groupMemberRecords" \ 0 \ "isRepMemberOfGroup").as[Int] mustBe 1
      (json \ "groupMemberRecords" \ 0 \ "dateOfJoining").as[String] mustBe "2026-03-31"
      (json \ "groupMemberRecords" \ 0 \ "dateOfLeaving").as[String] mustBe "2026-03-31"
      (json \ "groupMemberRecords" \ 0 \ "emailAddress").as[String] mustBe "a@b.com"

      (json \ "groupMemberRecords" \ 1 \ "mgdRegNumber").as[String] mustBe "XYM00000000000"
      (json \ "groupMemberRecords" \ 1 \ "businessPartnerNumber").as[String] mustBe "1100049911"
      (json \ "groupMemberRecords" \ 1 \ "businessName").as[String] mustBe "Oh It's A Business"
      (json \ "groupMemberRecords" \ 1 \ "crn").as[String] mustBe "19876544"
      (json \ "groupMemberRecords" \ 1 \ "foreignCorporateRef").as[String] mustBe "GGR-UK-187654"
      (json \ "groupMemberRecords" \ 1 \ "isRepMemberOfGroup").as[Int] mustBe 0
      (json \ "groupMemberRecords" \ 1 \ "dateOfJoining").as[String] mustBe "2026-05-31"
      (json \ "groupMemberRecords" \ 1 \ "dateOfLeaving").as[String] mustBe "2026-05-31"
      (json \ "groupMemberRecords" \ 1 \ "emailAddress").as[String] mustBe "d@e.com"



    }

    "deserialize from JSON correctly" in {
      val joinDate: LocalDate = LocalDate.of(2026, 3, 31)
      val leaveDate: LocalDate = LocalDate.of(2026, 6, 21)
      val systemDate: LocalDate = LocalDate.of(2026, 1, 1)

      val result: JsResult[GroupMembers] = validJson.validate[GroupMembers]

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
            )),
            Some(systemDate))
          )
    }

    "round-trip (write then read) should return same object" in {
      val original = GroupMembers(Seq(GroupMemberRecord("XYZ00000000021",None, None,None,None,None,None, None, None)), None)

      val json = Json.toJson(original)
      val parsed = json.as[GroupMembers]

      parsed mustBe original
    }

    "succeed when optional fields are missing" in {
      val validJsonWithMissingFields = Json.parse(
        """
          |{
          |   "groupMemberRecords": [
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
          |   "groupMemberRecords": [
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
          |   "groupMemberRecords": [
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
}
