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

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.BusinessType.SoleProprietor
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.{RecordNotFound, RepositoryError}

import java.time.LocalDate

object GamblingStubData {

  private val fixedDate = LocalDate.parse("2026-01-01")

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

      case "XER00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        ReturnSummary(mgdRegNumber, 3, 4)
    }

  def getTradeClassDetails(mgdRegNumber: String): TradeClassDetails = {
    mgdRegNumber match {
      case "XER00000000000" => throw new RuntimeException("Simulated downstream failure")
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
          mgdRegNumber      = mgdRegNumber,
          nameLine1         = Some("foo"),
          nameLine2         = Some("foo"),
          phoneNumber       = Some("07618728019"),
          mobilePhoneNumber = Some("018937617281"),
          faxNumber         = Some("foo"),
          emailAddr         = Some("foo@mail.com"),
          adi               = Some("none"),
          address1          = Some("random street"),
          address2          = Some("bar"),
          address3          = Some("bar"),
          address4          = Some("bar"),
          postcode          = Some("SR1 4DE"),
          country           = Some("Ingerland!"),
          iomOrCiFlag       = Some("true"),
          systemDate        = Some(LocalDate.now())
        )

      case "XER00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        CorrespondenceDetails(
          mgdRegNumber      = "UNKNOWN",
          nameLine1         = None,
          nameLine2         = None,
          phoneNumber       = None,
          mobilePhoneNumber = None,
          faxNumber         = None,
          emailAddr         = None,
          adi               = None,
          address1          = None,
          address2          = None,
          address3          = None,
          address4          = None,
          postcode          = None,
          country           = None,
          iomOrCiFlag       = None,
          systemDate        = Some(LocalDate.now())
        )
    }

  def getBusinessAddressDetails(
    mgdRegNumber: String
  ): BusinessAddressDetails =
    mgdRegNumber match {

      case "XYZ00000000001" =>
        BusinessAddressDetails(
          mgdRegNumber = mgdRegNumber,
          adi          = Some("none"),
          address1     = Some("random street"),
          address2     = Some("bar"),
          address3     = Some("bar"),
          address4     = Some("bar"),
          postcode     = Some("SR1 4DE"),
          country      = Some("Ingerland!"),
          iomOrCiFlag  = Some("true"),
          systemDate   = Some(LocalDate.now())
        )

      case "XER00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        BusinessAddressDetails(
          mgdRegNumber = "UNKNOWN",
          adi          = None,
          address1     = None,
          address2     = None,
          address3     = None,
          address4     = None,
          postcode     = None,
          country      = None,
          iomOrCiFlag  = None,
          systemDate   = Some(LocalDate.now())
        )
    }

  def getControllingBodyDetails(
    mgdRegNumber: String
  ): ControllingBodyDetails =
    mgdRegNumber match {

      case "XYZ00000000001" =>
        ControllingBodyDetails(
          mgdRegNumber           = mgdRegNumber,
          businessPartnerNumber  = Some("foo"),
          dateOfJoining          = Some(fixedDate),
          dateOfLeaving          = Some(fixedDate),
          solePropTitle          = Some("foo"),
          solePropFirstName      = Some("foo"),
          solePropMiddleName     = Some("foo"),
          solePropLastName       = Some("foo"),
          businessName           = Some("foo"),
          tradingName            = Some("foo"),
          dateOfBirth            = Some(fixedDate),
          nino                   = Some("foo"),
          utr                    = Some(1),
          vrn                    = Some(2),
          crn                    = Some("foo"),
          dateOfIncorporation    = Some(fixedDate),
          countryOfIncorporation = Some("foo"),
          foreignCorporateRef    = Some("foo"),
          address1               = Some("random street"),
          address2               = Some("bar"),
          address3               = Some("bar"),
          address4               = Some("foo"),
          postcode               = Some("SR1 4DE"),
          country                = Some("Ingerland!"),
          adi                    = Some("none"),
          isIomOrCiFlag          = Some("true"),
          phoneNumber            = Some("foo"),
          mobilePhoneNumber      = Some("foo"),
          faxNumber              = Some("foo"),
          emailAddr              = Some("foo"),
          typeOfControllingBody  = Some(SoleProprietor),
          isRepMemSameAsCb       = Some("foo"),
          isUkIncorporated       = Some("foo"),
          systemDate             = Some(fixedDate)
        )

      case "XER00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        ControllingBodyDetails(
          mgdRegNumber           = "",
          businessPartnerNumber  = None,
          dateOfJoining          = None,
          dateOfLeaving          = None,
          solePropTitle          = None,
          solePropFirstName      = None,
          solePropMiddleName     = None,
          solePropLastName       = None,
          businessName           = None,
          tradingName            = None,
          dateOfBirth            = None,
          nino                   = None,
          utr                    = None,
          vrn                    = None,
          crn                    = None,
          dateOfIncorporation    = None,
          countryOfIncorporation = None,
          foreignCorporateRef    = None,
          address1               = None,
          address2               = None,
          address3               = None,
          address4               = None,
          postcode               = None,
          country                = None,
          adi                    = None,
          isIomOrCiFlag          = None,
          phoneNumber            = None,
          mobilePhoneNumber      = None,
          faxNumber              = None,
          emailAddr              = None,
          typeOfControllingBody  = None,
          isRepMemSameAsCb       = None,
          isUkIncorporated       = None,
          systemDate             = None
        )
    }

  def getPremisesDetails(
    MgdRegNumber: String
  ): PremisesDetailsResponse =
    MgdRegNumber match {

      case "XYZ00000000001" =>
        PremisesDetailsResponse(
          totalRows = Some(1000),
          premises = Seq(
            PremisesDetails(
              mgdRegNumber = "XYZ00000000001",
              address1     = Some("Flat 55"),
              address2     = Some("20 Market Calle"),
              address3     = Some("Barcelona"),
              address4     = None,
              postcode     = None,
              Some(fixedDate)
            ),
            PremisesDetails(
              mgdRegNumber = "XYZ00000000001",
              address1     = Some("Flat 1"),
              address2     = Some("10 Market Calle"),
              address3     = Some("Madrid"),
              address4     = None,
              postcode     = None,
              Some(fixedDate)
            )
          )
        )

      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")

      case _ =>
        PremisesDetailsResponse(totalRows = Some(0),
                                premises = Seq(
                                )
                               )
    }

  // -------------------------
  // OperatorDetails
  // -------------------------
  def getOperatorDetails(mgdRegNumber: String): OperatorDetails =
    mgdRegNumber match {

      case "XER00000000000" =>
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
      case "XER00000000000" =>
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

      case "XER00000000000" =>
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
      case "XER00000000000" =>
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

      case "XER00000000000" =>
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

      case "XER00000000000" =>
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

  def getPartnerDetailsData(regNumber: String): PartnerDetails =
    regNumber match {
      case "XYM00000000000" =>
        PartnerDetails(
          partners = List(
            Partner(
              mgdRegNumber           = "XYM00000000000",
              businessPartnerNumber  = Some("0100049899"),
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
              businessPartnerNumber  = Some("0100049899"),
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
      case "XEM33333333333" => throw new RuntimeException("Simulated downstream failure")
    }

  def getGroupMembersData(regNumber: String): GroupMembers =
    regNumber match {
      case "XYM00000000000" =>
        GroupMembers(groupMembers =
          List(
            GroupMemberRecord(
              mgdRegNumber          = "XYM00000000004",
              businessPartnerNumber = Some("0100049899"),
              businessName          = Some("BusinessCo"),
              crn                   = Some("09876543"),
              foreignCorporateRef   = Some("FCR-UK-987654"),
              isRepMemberOfGroup    = Some(1),
              dateOfJoining         = Some(LocalDate.of(2025, 1, 1)),
              dateOfLeaving         = Some(LocalDate.of(2025, 1, 1)),
              emailAddr             = Some("a@b.com"),
              systemDate            = Some(LocalDate.of(2026, 10, 31))
            ),
            GroupMemberRecord(
              mgdRegNumber          = "XYM00000000000",
              businessPartnerNumber = Some("1100049911"),
              businessName          = Some("Oh It's A Business"),
              crn                   = Some("19876544"),
              foreignCorporateRef   = Some("GGR-UK-187654"),
              isRepMemberOfGroup    = Some(0),
              dateOfJoining         = Some(LocalDate.of(2025, 9, 9)),
              dateOfLeaving         = Some(LocalDate.of(2025, 9, 9)),
              emailAddr             = Some("d@e.com"),
              systemDate            = Some(LocalDate.of(2026, 10, 31))
            )
          )
        )
      case "XYZ00000000001" =>
        GroupMembers(groupMembers =
          List(
            GroupMemberRecord(
              mgdRegNumber          = "XYM00000000001",
              businessPartnerNumber = Some("2186744422"),
              businessName          = Some("Mr Business"),
              crn                   = Some("11176543"),
              foreignCorporateRef   = Some("ACD-UK-222654"),
              isRepMemberOfGroup    = Some(1),
              dateOfJoining         = Some(LocalDate.of(2021, 1, 1)),
              dateOfLeaving         = Some(LocalDate.of(2026, 1, 1)),
              emailAddr             = Some("f@g.com"),
              systemDate            = Some(LocalDate.of(2026, 10, 31))
            )
          )
        )
      case "XEM33333333333" => throw new RuntimeException("Simulated downstream failure")
    }

  def getMemberDetails(regime: Regime, regNumber: String, partnerNumber: String): Either[RepositoryError, MemberDetails] =
    (regNumber, partnerNumber) match {
      case ("XYM00000000000", "0100049899") =>
        Right(MemberDetails(
          mgdRegNumber = "XYM00000000000",
          businessPartnerNumber = "0100049899",
          businessName = Some("Business Name"),
          tradingName = Some("Trading Name"),
          crn = Some("09876543"),
          dateOfIncorporation = Some(LocalDate.of(2025, 1, 1)),
          countryOfIncorporation = Some("UK"),
          foreignCorporateRef = Some("FCR-UK-987654"),
          utr = Some(1234567890),
          vrn = Some(123456789),
          tradeClass = Some(3),
          businessActivityDesc = Some("Business Activities"),
          phoneNumber = Some("0800 123 456"),
          mobilePhoneNumber = Some("07900 678 900"),
          faxNumber = Some("No one uses fax anymore"),
          emailAddr = Some("a@b.com"),
          adi = Some("our friend adi"),
          address1 = Some("123 Road"),
          address2 = Some("Place"),
          address3 = Some("More info about this place"),
          address4 = Some("Even more? noo"),
          postcode = Some("SA0 1AH"),
          isIomOrCiFlag = Some("1"),
          haveGamblingLicenceNo = Some("0"),
          gamblingLicenceNo = Some("1"),
          heldByLandlord = Some("0"),
          localAuthority = Some("1"),
          familyEntertainment = Some("0"),
          clubGaming = Some("1"),
          clubLicence = Some("0"),
          prizeGaming = Some("1"),
          onPremises = Some("0"),
          clubPremises = Some("1"),
          regCert = Some("0"),
          bookmaking = Some("1"),
          bingo = Some("0"),
          amusement = Some("1"),
          serveAlcohol = Some("0"),
          premisesNotCovered = Some("1"),
          previousMgdrn1 = Some("XVM12345678800"),
          previousMgdrn2 = Some("XVM12345678801"),
          previousMgdrn3 = Some("XVM12345678802"),
          associatedMgdrn1 = Some("XGM12345678800"),
          associatedMgdrn2 = Some("XGM12345678801"),
          associatedMgdrn3 = Some("XGM123456788002"),
          dateOfJoining = Some(LocalDate.of(2026, 1, 16)),
          dateOfLeaving = Some(LocalDate.of(2026, 10, 16)),
          isRepMemSameAsCb = Some("1"),
          isRepMemberOfGroup = Some("1"),
          isUkIncorporated = Some("1"),
          systemDate = Some(LocalDate.of(2026, 11, 16)),
          premises = Seq(
            PremisesDetails(
              mgdRegNumber = "XWM12345678901",
              address1 = Some("123 Road"),
              address2 = Some("Place"),
              address3 = Some("More info about this place"),
              address4 = Some("Even more? noo"),
              postcode = Some("SA0 1AH"),
              systemDate = Some(LocalDate.of(2026, 11, 16))
            ),
            PremisesDetails(
              mgdRegNumber = "XWM12345678901",
              address1 = Some("456 Road"),
              address2 = Some("Zone"),
              address3 = None,
              address4 = None,
              postcode = Some("BA0 1HA"),
              systemDate = Some(LocalDate.of(2026, 11, 16))
            )
          )
        )
        )
      case ("XYM00000000001", "0100049888") =>
        Right(MemberDetails(
          mgdRegNumber = "XYM00000000001",
          businessPartnerNumber = "0100049888",
          businessName = Some("Business Joe"),
          tradingName = Some("Trading Joe"),
          crn = Some("09876543"),
          dateOfIncorporation = Some(LocalDate.of(2026, 1, 1)),
          countryOfIncorporation = Some("USA"),
          foreignCorporateRef = Some("FCR-USA-987654"),
          utr = Some(1234567811),
          vrn = Some(123456119),
          tradeClass = Some(1),
          businessActivityDesc = Some("Business"),
          phoneNumber = Some("0800 123 111"),
          mobilePhoneNumber = Some("07900 678 111"),
          faxNumber = Some("NO FAX FR"),
          emailAddr = Some("b@a.com"),
          adi = Some("additional info: I am tired"),
          address1 = Some("234 Road"),
          address2 = Some("Plaice (the fish)"),
          address3 = Some("More info about this plaice"),
          address4 = None,
          postcode = None,
          isIomOrCiFlag = Some("0"),
          haveGamblingLicenceNo = Some("1"),
          gamblingLicenceNo = Some("0"),
          heldByLandlord = Some("1"),
          localAuthority = Some("0"),
          familyEntertainment = Some("1"),
          clubGaming = Some("0"),
          clubLicence = Some("1"),
          prizeGaming = Some("0"),
          onPremises = Some("1"),
          clubPremises = Some("0"),
          regCert = Some("1"),
          bookmaking = Some("0"),
          bingo = Some("1"),
          amusement = Some("0"),
          serveAlcohol = Some("1"),
          premisesNotCovered = Some("0"),
          previousMgdrn1 = Some("XVM12345678805"),
          previousMgdrn2 = Some("XVM12345678805"),
          previousMgdrn3 = Some("XVM13345678805"),
          associatedMgdrn1 = Some("XGM12345678803"),
          associatedMgdrn2 = Some("XGM12345678801"),
          associatedMgdrn3 = Some("XGM12345678802"),
          dateOfJoining = Some(LocalDate.of(2026, 10, 16)),
          dateOfLeaving = Some(LocalDate.of(2026, 1, 16)),
          isRepMemSameAsCb = Some("0"),
          isRepMemberOfGroup = Some("0"),
          isUkIncorporated = Some("0"),
          systemDate = Some(LocalDate.of(2026, 2, 16)),
          premises = Seq(
            PremisesDetails(
              mgdRegNumber = "XWM12345678901",
              address1 = Some("123 Road"),
              address2 = Some("Place"),
              address3 = Some("More info about this place"),
              address4 = Some("Even more? noo"),
              postcode = Some("SA0 1AH"),
              systemDate = Some(LocalDate.of(2026, 11, 16))
            )
          )
        ))
      case ("XEM33333333333", _) => throw new RuntimeException("Simulated downstream failure")
      case unknown =>
        Left(RecordNotFound(s"Unknown registration number $regNumber"))
    }

  def getReturnPeriods(regNumber: String, isRight: Boolean = true): Either[RepositoryError, ReturnPeriods] =
    if (!isRight) {
      Left(RecordNotFound(s"Unknown ref numbe$regNumber"))
    } else {
      regNumber match {
        case "XYM00000000000" =>
          Right(
            ReturnPeriods(
              mgdRegNumber          = "XYM00000000000",
              returnPeriodsId       = Some(1),
              nstpEndDate1          = Some(LocalDate.of(2024, 10, 14)),
              nstpEndDate2          = Some(LocalDate.of(2025, 1, 14)),
              nstpEndDate3          = Some(LocalDate.of(2025, 4, 15)),
              nstpEndDate4          = Some(LocalDate.of(2025, 7, 15)),
              nstpEndDate5          = Some(LocalDate.of(2025, 10, 14)),
              nstpEndDate6          = Some(LocalDate.of(2026, 1, 14)),
              nstpEndDate7          = Some(LocalDate.of(2026, 4, 15)),
              nstpEndDate8          = Some(LocalDate.of(2026, 7, 17)),
              isInLastNstp          = Some("1"),
              finalPeriodWarning    = Some("0"),
              hasExistingNstpValues = Some("1"),
              systemDate            = Some(LocalDate.of(2026, 5, 31))
            )
          )

        case "XYZ00000000001" =>
          Right(
            ReturnPeriods(
              mgdRegNumber          = "XYZ00000000001",
              returnPeriodsId       = None,
              nstpEndDate1          = None,
              nstpEndDate2          = None,
              nstpEndDate3          = None,
              nstpEndDate4          = None,
              nstpEndDate5          = None,
              nstpEndDate6          = None,
              nstpEndDate7          = None,
              nstpEndDate8          = None,
              isInLastNstp          = None,
              finalPeriodWarning    = None,
              hasExistingNstpValues = None,
              systemDate            = None
            )
          )

        case "XEM33333333333" =>
          throw new RuntimeException("Simulated downstream failure")

        case unknown =>
          Left(RecordNotFound(s"Unknown registration number: $unknown"))
      }
    }

  def getAgentDetails(agentReference: String): Either[RepositoryError, AgentDetails] =
    agentReference match {
      case "AGENT001" =>
        Right(
          AgentDetails(
            businessName      = Some("Gambling company 1"),
            addressLine1      = Some("1"),
            addressLine2      = Some("Example street"),
            addressLine3      = Some("Town"),
            addressLine4      = Some("County"),
            postcode          = Some("SW1A 1AA"),
            country           = Some("United Kingdom"),
            abroadSignal      = Some("N"),
            phoneNumber       = Some("02079460000"),
            mobilePhoneNumber = Some("07700900999"),
            faxNumber         = Some("02079460123"),
            email             = Some("user@example.com")
          )
        )

      case "AGENTERR" =>
        throw new RuntimeException("Simulated downstream failure")

      case unknown =>
        Left(RecordNotFound(s"Unknown agent reference: $unknown"))
    }
}
