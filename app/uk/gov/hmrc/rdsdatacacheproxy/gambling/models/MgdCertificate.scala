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

import play.api.libs.json.{Json, OFormat, Writes}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class PartnerMember(
  namesOfPartMems: String,
  solePropTitle: Option[String],
  solePropFirstName: Option[String],
  solePropMiddleName: Option[String],
  solePropLastName: Option[String],
  typeOfBusiness: Int // 1..5 mapping
)

object PartnerMember {
  implicit val format: OFormat[PartnerMember] = Json.format[PartnerMember]
}

final case class GroupMember(
  namesOfGroupMems: String
)

object GroupMember {
  implicit val format: OFormat[GroupMember] = Json.format[GroupMember]
}

final case class ReturnPeriodEndDate(
  returnPeriodEndDate: LocalDate
)

object ReturnPeriodEndDate {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[ReturnPeriodEndDate] = Json.format[ReturnPeriodEndDate]
}

final case class MgdCertificate(
  mgdRegNumber: String,
  registrationDate: Option[LocalDate],
  individualName: Option[String],
  businessName: Option[String],
  tradingName: Option[String],
  repMemName: Option[String],
  busAddrLine1: Option[String],
  busAddrLine2: Option[String],
  busAddrLine3: Option[String],
  busAddrLine4: Option[String],
  busPostcode: Option[String],
  busCountry: Option[String],
  busAdi: Option[String],
  repMemLine1: Option[String],
  repMemLine2: Option[String],
  repMemLine3: Option[String],
  repMemLine4: Option[String],
  repMemPostcode: Option[String],
  repMemAdi: Option[String],
  typeOfBusiness: Option[String],
  businessTradeClass: Option[Int],
  noOfPartners: Option[Int],
  groupReg: String, // "Y" | "N"
  noOfGroupMems: Option[Int],
  dateCertIssued: Option[LocalDate],
  partMembers: Seq[PartnerMember],
  groupMembers: Seq[GroupMember],
  returnPeriodEndDates: Seq[ReturnPeriodEndDate]
)

object MgdCertificate {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[MgdCertificate] = Json.format[MgdCertificate]
}
