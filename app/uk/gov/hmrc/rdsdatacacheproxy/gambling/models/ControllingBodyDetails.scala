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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class ControllingBodyDetails(
  mgdRegNumber: String,
  business_Partner_Number: Option[String],
  date_Of_Joining: Option[LocalDate],
  date_Of_Leaving: Option[LocalDate],
  sole_Prop_Title: Option[String],
  sole_Prop_First_Name: Option[String],
  sole_Prop_Middle_Name: Option[String],
  sole_Prop_Last_Name: Option[String],
  business_Name: Option[String],
  trading_Name: Option[String],
  date_Of_Birth: Option[LocalDate],
  nino: Option[String],
  utr: Option[Int],
  vrn: Option[Int],
  crn: Option[String],
  date_Of_Incorporation: Option[LocalDate],
  country_Of_Incorporation: Option[String],
  foreign_Corporate_Ref: Option[String],
  address_1: Option[String],
  address_2: Option[String],
  address_3: Option[String],
  address_4: Option[String],
  postcode: Option[String],
  country: Option[String],
  adi: Option[String],
  is_Iom_Or_Ci: Option[String],
  phone_Number: Option[String],
  mobile_Phone_Number: Option[String],
  fax_Number: Option[String],
  email_Addr: Option[String],
  type_Of_Controlling_Body: Option[Int],
  is_Rep_Mem_Same_As_Cb: Option[String],
  is_Uk_Incorporated: Option[String],
  systemDate: Option[LocalDate]
)

object ControllingBodyDetails {
  implicit val format: OFormat[ControllingBodyDetails] =
    Json.format[ControllingBodyDetails]
}
