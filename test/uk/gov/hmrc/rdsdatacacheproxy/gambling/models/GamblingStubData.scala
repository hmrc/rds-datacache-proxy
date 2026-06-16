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

  def getTradeClassDetails(mgdRegNumber: String): TradeClassDetails = {
    mgdRegNumber match {
      case "ERR00000000000" => throw new RuntimeException("Simulated downstream failure")
      case _ =>
        TradeClassDetails(
          mgdRegNumber         = mgdRegNumber,
          businessTradeClass   = Some(1),
          businessActivityDesc = "Gaming Machine Operation",
          systemDate           = Some(LocalDate.of(2026, 5, 31))
        )
    }
  }

  def getCorrespondenceDetails(
                                 mgdRegNumber: String
                               ): CorrespondenceDetails =
    mgdRegNumber match {

      case "XYZ00000000001" =>

        CorrespondenceDetails(
          mgdRegNumber = mgdRegNumber,
          nameLine1 = Some("foo"),
          nameLine2 = Some("foo"),
          phoneNumber = Some("07618728019"),
          mobilePhoneNumber = Some("018937617281"),
          faxNumber = Some("foo"),
          emailAddr = Some("foo@mail.com"),
          adi = Some("none"),
          address1 = Some("random street"),
          address2 = Some("bar"),
          address3 = Some("bar"),
          address4 = Some("bar"),
          postcode = Some("SR1 4DE"),
          country = Some("Ingerland!"),
          iomOrCiFlag = Some("true"),
          systemDate = Some(LocalDate.now())
        )

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        CorrespondenceDetails(
          mgdRegNumber = "UNKNOWN",
          nameLine1 = None,
          nameLine2 = None,
          phoneNumber = None,
          mobilePhoneNumber = None,
          faxNumber = None,
          emailAddr = None,
          adi = None,
          address1 = None,
          address2 = None,
          address3 = None,
          address4 = None,
          postcode = None,
          country = None,
          iomOrCiFlag = None,
          systemDate = Some(LocalDate.now())
        )
    }
  

  // -------------------------
  // OperatorDetails
  // -------------------------
  def getOperatorDetails(mgdRegNumber: String): OperatorDetails =
    mgdRegNumber match {

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case "EMPTY000000000" =>
        throw new RuntimeException("No data")

      case "NULL000000000" =>
        throw new RuntimeException("Null cursor")

      case _ =>
        OperatorDetails(
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

      case "NULL000000000" =>
        throw new RuntimeException("Null cursor")

      case "XYZ00000000000" =>
        BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = Some(BusinessType.CorporateBody),
          currentlyRegistered   = 2,
          groupReg              = false,
          dateOfRegistration    = Some(LocalDate.of(2024, 4, 21)),
          businessPartnerNumber = Some("bar"),
          systemDate            = LocalDate.of(2024, 4, 21)
        )
      case "XYZ00000000001" =>
        BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = None,
          currentlyRegistered   = 1,
          groupReg              = false,
          dateOfRegistration    = Some(LocalDate.of(2024, 4, 21)),
          businessPartnerNumber = Some("bar"),
          systemDate            = LocalDate.of(2024, 4, 21)
        )
      case "XYZ00000000010" =>
        BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = Some(BusinessType.UnincorporatedBody),
          currentlyRegistered   = 2,
          groupReg              = true,
          dateOfRegistration    = Some(LocalDate.of(2024, 4, 21)),
          businessPartnerNumber = Some("bar"),
          systemDate            = LocalDate.of(2024, 4, 21)
        )
      case "XYZ00000000012" =>
        BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = Some(BusinessType.SoleProprietor),
          currentlyRegistered   = 2,
          groupReg              = true,
          dateOfRegistration    = Some(LocalDate.of(2023, 4, 21)),
          businessPartnerNumber = Some("barfoo"),
          systemDate            = LocalDate.of(2023, 4, 21)
        )
      case "XYZ00000000021" =>
        BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = Some(BusinessType.Partnership),
          currentlyRegistered   = 2,
          groupReg              = false,
          dateOfRegistration    = Some(LocalDate.of(2024, 1, 21)),
          businessPartnerNumber = Some("barbar"),
          systemDate            = LocalDate.of(2024, 1, 21)
        )
      case _ =>
        BusinessDetails(
          mgdRegNumber          = mgdRegNumber,
          businessType          = Some(BusinessType.Partnership),
          currentlyRegistered   = 0,
          groupReg              = false,
          dateOfRegistration    = Some(LocalDate.of(2026, 4, 22)),
          businessPartnerNumber = Some("unknown"),
          systemDate            = LocalDate.of(2026, 4, 22)
        )
    }

  def getMgdDetails(mgdRegNumber: String): MgdDetails =
    mgdRegNumber match {

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case "EMPTY000000000" =>
        MgdDetails(
          mgdRegNumber       = "",
          isBusinessSeasonal = None,
          previousMgdrn1     = None,
          previousMgdrn2     = None,
          previousMgdrn3     = None,
          associatedMgdrn1   = None,
          associatedMgdrn2   = None,
          associatedMgdrn3   = None,
          systemDate         = None
        )

      case _ =>
        MgdDetails(
          mgdRegNumber       = mgdRegNumber,
          isBusinessSeasonal = Some(1),
          previousMgdrn1     = Some("PREV001"),
          previousMgdrn2     = Some("PREV002"),
          previousMgdrn3     = None,
          associatedMgdrn1   = Some("ASSOC001"),
          associatedMgdrn2   = Some("ASSOC002"),
          associatedMgdrn3   = None,
          systemDate         = Some(LocalDate.of(2026, 5, 31))
        )
    }

  def getBusinessName(mgdRegNumber: String): BusinessName = {
    val dateTimeOne: Some[LocalDate] = Some(LocalDate.of(2026, 4, 20))
    val dateTimeTwo: Some[LocalDate] = Some(LocalDate.of(2026, 1, 1))
    val dateTimeThree: Some[LocalDate] = Some(LocalDate.of(1991, 1, 1))
    mgdRegNumber match {
      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case "EMPTY000000000" =>
        throw new RuntimeException("No business name found")

      case "NULL000000000" =>
        throw new RuntimeException("Null cursor")

      case "XYZ00000000000" =>
        BusinessName(
          mgdRegNumber,
          solePropTitle     = Some("Mr"),
          solePropFirstName = Some("John"),
          solePropMidName   = Some("C"),
          solePropLastName  = Some("Doe"),
          businessName      = Some("John Doe Co."),
          businessType      = Some(BusinessType.SoleProprietor),
          tradingName       = Some("DoeDoe"),
          systemDate        = dateTimeOne
        )
      case "XYZ00000000001" =>
        BusinessName(
          mgdRegNumber,
          solePropTitle     = Some("Mrs"),
          solePropFirstName = Some("Jane"),
          solePropMidName   = Some("C"),
          solePropLastName  = Some("Doe"),
          businessName      = Some("Jane Doe Co."),
          businessType      = Some(BusinessType.SoleProprietor),
          tradingName       = Some("DoeDoe"),
          systemDate        = dateTimeTwo
        )
      case "XYZ00000000010" =>
        BusinessName(
          mgdRegNumber,
          solePropTitle     = Some("Mrs"),
          solePropFirstName = Some("Marge"),
          solePropMidName   = Some("Jacqueline"),
          solePropLastName  = Some("Simpson"),
          businessName      = Some("Pretzel Wagon"),
          businessType      = Some(BusinessType.SoleProprietor),
          tradingName       = Some("Marge Simpson"),
          systemDate        = dateTimeOne
        )
      case "XYZ00000000012" =>
        BusinessName(
          mgdRegNumber,
          solePropTitle     = Some("Miss"),
          solePropFirstName = Some("Catherine"),
          solePropMidName   = None,
          solePropLastName  = Some("Havisham"),
          businessName      = Some("Failed Expectations"),
          businessType      = Some(BusinessType.SoleProprietor),
          tradingName       = Some("Miss Havisham"),
          systemDate        = dateTimeThree
        )
      case "XYZ00000000021" =>
        BusinessName(
          mgdRegNumber,
          solePropTitle     = Some("Mr"),
          solePropFirstName = Some("Eugine"),
          solePropMidName   = Some("H"),
          solePropLastName  = Some("Krabs"),
          businessName      = Some("Krusty Krab"),
          businessType      = Some(BusinessType.SoleProprietor),
          tradingName       = Some("Mr Krabs"),
          systemDate        = dateTimeThree
        )
      case _ =>
        BusinessName(
          mgdRegNumber,
          solePropTitle     = Some("Mr"),
          solePropFirstName = Some("Foo"),
          solePropMidName   = Some("B"),
          solePropLastName  = Some("Bar"),
          businessName      = Some("FooBar Co."),
          businessType      = Some(BusinessType.SoleProprietor),
          tradingName       = Some("Foobar"),
          systemDate        = dateTimeOne
        )
    }
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

  def getBusinessContactDetails(
    mgdRegNumber: String
  ): BusinessContactDetails =
    mgdRegNumber match {

      case "XYZ00000000001" =>
        BusinessContactDetails(
          mgdRegNumber      = "XYZ00000000001",
          phoneNumber       = Some("0123456789"),
          mobilePhoneNumber = Some("07123456789"),
          faxNumber         = Some("0201234567"),
          emailAddr         = Some("test@test.com"),
          systemDate        = Some(LocalDate.of(2026, 4, 20))
        )

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        BusinessContactDetails(
          mgdRegNumber      = mgdRegNumber,
          phoneNumber       = None,
          mobilePhoneNumber = None,
          faxNumber         = None,
          emailAddr         = None,
          systemDate        = None
        )
    }
}
