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

import play.api.libs.json.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class GroupMembers(groupMembers: Seq[GroupMemberRecord])

final case class GroupMemberRecord(mgdRegNumber: String,
                                   businessPartnerNumber: Option[String],
                                   businessName: Option[String],
                                   crn: Option[String],
                                   foreignCorporateRef: Option[String],
                                   isRepMemberOfGroup: Option[Int],
                                   dateOfJoining: Option[LocalDate],
                                   dateOfLeaving: Option[LocalDate],
                                   emailAddr: Option[String],
                                   systemDate: Option[LocalDate]
                                  )

final case class MemberDetails(
  mgdRegNumber: String,
  businessPartnerNumber: String,
  businessName: Option[String],
  tradingName: Option[String],
  crn: Option[String],
  dateOfIncorporation: Option[LocalDate],
  countryOfIncorporation: Option[String],
  foreignCorporateRef: Option[String],
  utr: Option[Int],
  vrn: Option[Int],
  tradeClass: Option[Int],
  businessActivityDesc: Option[String],
  phoneNumber: Option[String],
  mobilePhoneNumber: Option[String],
  faxNumber: Option[String],
  emailAddr: Option[String],
  adi: Option[String],
  address1: Option[String],
  address2: Option[String],
  address3: Option[String],
  address4: Option[String],
  postcode: Option[String],
  isIomOrCiFlag: Option[String],
  haveGamblingLicenceNo: Option[String],
  gamblingLicenceNo: Option[String],
  heldByLandlord: Option[String],
  localAuthority: Option[String],
  familyEntertainment: Option[String],
  clubGaming: Option[String],
  clubLicence: Option[String],
  prizeGaming: Option[String],
  onPremises: Option[String],
  clubPremises: Option[String],
  regCert: Option[String],
  bookmaking: Option[String],
  bingo: Option[String],
  amusement: Option[String],
  serveAlcohol: Option[String],
  premisesNotCovered: Option[String],
  previousMgdrn1: Option[String],
  previousMgdrn2: Option[String],
  previousMgdrn3: Option[String],
  associatedMgdrn1: Option[String],
  associatedMgdrn2: Option[String],
  associatedMgdrn3: Option[String],
  dateOfJoining: Option[LocalDate],
  dateOfLeaving: Option[LocalDate],
  isRepMemSameAsCb: Option[String],
  isRepMemberOfGroup: Option[String],
  isUkIncorporated: Option[String],
  systemDate: Option[LocalDate],
  premises: Seq[PremisesDetails]
)

object GroupMemberRecord {

  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[GroupMemberRecord] =
    Json.format[GroupMemberRecord]
}

object GroupMembers {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[GroupMembers] =
    Json.format[GroupMembers]
}

object MemberDetails {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[MemberDetails] =
    Json.format[MemberDetails]
}
