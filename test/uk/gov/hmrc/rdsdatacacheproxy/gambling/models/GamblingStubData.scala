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

import java.time.LocalDate

object GamblingStubData {

  // -------------------------
  // ReturnSummary
  // -------------------------
  def getReturnSummary(mgdRegNumber: String): ReturnSummary =
    mgdRegNumber match {

      case "XYZ00000000000" => ReturnSummary(mgdRegNumber, 0, 0)
      case "XYZ00000000001" => ReturnSummary(mgdRegNumber, 0, 1)
      case "XYZ00000000010" => ReturnSummary(mgdRegNumber, 1, 0)
      case "XYZ00000000012" => ReturnSummary(mgdRegNumber, 1, 2)
      case "XYZ00000000021" => ReturnSummary(mgdRegNumber, 2, 1)

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        ReturnSummary(mgdRegNumber, 3, 4)
    }

  // -------------------------
  // OperatorDetails
  // -------------------------
  def getOperatorDetails(mgdRegNumber: String): GetOperatorDetails =
    mgdRegNumber match {

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case "EMPTY000000000" =>
        throw new RuntimeException("No data")

      case "NULL000000000" =>
        throw new RuntimeException("Null cursor")

      case _ =>
        GetOperatorDetails(
          mgdRegNumber       = mgdRegNumber,
          solePropName       = Some("John Smith"),
          solePropTitle      = Some("Mr"),
          solePropFirstName  = Some("John"),
          solePropMiddleName = None,
          solePropLastName   = Some("Smith"),
          tradingName        = Some("Test Trading"),
          businessName       = Some("Test Business Ltd"),
          businessType       = Some(1),
          adi                = Some("ADI123"),
          address1           = Some("Line 1"),
          address2           = Some("Line 2"),
          address3           = None,
          address4           = None,
          postcode           = Some("AB1 2CD"),
          country            = Some("UK"),
          abroadSig          = None,
          agentOwnRef        = Some("AGENT123"),
          systemDate         = Some(LocalDate.now())
        )
    }

  // -------------------------
  // BusinessDetails
  // -------------------------
  def getBusinessDetails(mgdRegNumber: String): BusinessDetails =
    mgdRegNumber match {

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case "EMPTY000000000" =>
        throw new RuntimeException("No business details found")

      case _ =>
        BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = None,
          currentlyRegistered   = 1,
          isGroupMember         = false,
          dateOfRegistration    = Some(LocalDate.now().minusYears(1)),
          businessPartnerNumber = Some("BPN123456"),
          systemDate            = LocalDate.now()
        )
    }

  // -------------------------
  // MgdCertificate
  // -------------------------
  def getMgdCertificate(mgdRegNumber: String): MgdCertificate =
    mgdRegNumber match {

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        MgdCertificate(
          mgdRegNumber         = mgdRegNumber,
          registrationDate     = Some(LocalDate.now().minusYears(2)),
          individualName       = Some("John Smith"),
          businessName         = Some("Test Business Ltd"),
          tradingName          = Some("Test Trading"),
          repMemName           = None,
          busAddrLine1         = Some("Line 1"),
          busAddrLine2         = None,
          busAddrLine3         = None,
          busAddrLine4         = None,
          busPostcode          = Some("AB1 2CD"),
          busCountry           = Some("UK"),
          busAdi               = None,
          repMemLine1          = None,
          repMemLine2          = None,
          repMemLine3          = None,
          repMemLine4          = None,
          repMemPostcode       = None,
          repMemAdi            = None,
          typeOfBusiness       = Some("Gambling"),
          businessTradeClass   = Some(1),
          noOfPartners         = Some(2),
          groupReg             = "N",
          noOfGroupMems        = Some(0),
          dateCertIssued       = Some(LocalDate.now()),
          partMembers          = Seq.empty,
          groupMembers         = Seq.empty,
          returnPeriodEndDates = Seq.empty
        )
    }
}
