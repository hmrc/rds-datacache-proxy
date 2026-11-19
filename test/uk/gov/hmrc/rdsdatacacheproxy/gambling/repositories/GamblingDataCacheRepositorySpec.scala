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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.{BeforeAndAfter, EitherValues}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.{DatabaseError, RecordNotFound, RepositoryError}

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingDataCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with EitherValues {

  var db: Database = _
  var repository: GamblingDataCacheRepository = _
  var mockConnection: Connection = _
  var mockCs: CallableStatement = _
  var returnSummaryRs: ResultSet = _
  var partRs: ResultSet = _
  var groupRs: ResultSet = _
  var returnPeriodRs: ResultSet = _
  var businessRs: ResultSet = _
  var operatorRs: ResultSet = _
  var mgdRs: ResultSet = _
  var tradeClassRs: ResultSet = _
  var businessContactRs: ResultSet = _
  var correspondenceRs: ResultSet = _
  var businessAddressRs: ResultSet = _
  var premisesRs: ResultSet = _
  var groupMembersRs: ResultSet = _
  var memberRs: ResultSet = _
  var agentAddressRs: ResultSet = _
  var agentContactRs: ResultSet = _

  before {
    db                = mock(classOf[Database])
    mockConnection    = mock(classOf[Connection])
    mockCs            = mock(classOf[CallableStatement])
    returnSummaryRs   = mock(classOf[ResultSet])
    partRs            = mock(classOf[ResultSet])
    groupRs           = mock(classOf[ResultSet])
    returnPeriodRs    = mock(classOf[ResultSet])
    businessRs        = mock(classOf[ResultSet])
    operatorRs        = mock(classOf[ResultSet])
    mgdRs             = mock(classOf[ResultSet])
    tradeClassRs      = mock(classOf[ResultSet])
    businessContactRs = mock(classOf[ResultSet])
    correspondenceRs  = mock(classOf[ResultSet])
    businessAddressRs = mock(classOf[ResultSet])
    premisesRs        = mock(classOf[ResultSet])
    agentAddressRs    = mock(classOf[ResultSet])
    agentContactRs    = mock(classOf[ResultSet])
    groupMembersRs    = mock(classOf[ResultSet])
    memberRs          = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCs)

    repository = new GamblingDataCacheRepository(db)
  }

  "getTradeClassDetails" should "return TradeClassDetails when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(tradeClassRs)
    when(tradeClassRs.next()).thenReturn(true)

    when(tradeClassRs.getString("mgd_reg_number")).thenReturn(mgdRegNumber)
    when(tradeClassRs.getObject("business_trade_class")).thenReturn(BigDecimal(3))
    when(tradeClassRs.getString("business_activity_desc")).thenReturn("Arcade Operator")
    when(tradeClassRs.getDate("system_date")).thenReturn(Date.valueOf("2025-01-01"))

    val result =
      repository.getTradeClassDetails(mgdRegNumber).futureValue

    result shouldBe TradeClassDetails(
      mgdRegNumber         = mgdRegNumber,
      businessTradeClass   = Some(3),
      businessActivityDesc = "Arcade Operator",
      systemDate           = Some(LocalDate.of(2025, 1, 1))
    )

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(tradeClassRs).close()
    verify(mockCs).close()
  }

  it should "return default TradeClassDetails when cursor is null" in {

    when(mockCs.getObject(2)).thenReturn(null)

    val result =
      repository.getTradeClassDetails("XWM00000001770").futureValue

    result shouldBe TradeClassDetails(
      mgdRegNumber         = "",
      businessTradeClass   = None,
      businessActivityDesc = "",
      systemDate           = None
    )

    verify(mockCs).close()
  }

  it should "return default TradeClassDetails when result set is empty" in {

    when(mockCs.getObject(2)).thenReturn(tradeClassRs)
    when(tradeClassRs.next()).thenReturn(false)

    val result =
      repository.getTradeClassDetails("XWM00000001770").futureValue

    result shouldBe TradeClassDetails(
      mgdRegNumber         = "",
      businessTradeClass   = None,
      businessActivityDesc = "",
      systemDate           = None
    )

    verify(tradeClassRs).close()
    verify(mockCs).close()
  }

  it should "handle null values safely" in {

    when(mockCs.getObject(2)).thenReturn(tradeClassRs)
    when(tradeClassRs.next()).thenReturn(true)

    when(tradeClassRs.getString("mgd_reg_number")).thenReturn("XWM00000001770")
    when(tradeClassRs.getObject("business_trade_class")).thenReturn(null)
    when(tradeClassRs.getString("business_activity_desc")).thenReturn(null)
    when(tradeClassRs.getDate("system_date")).thenReturn(null)

    val result =
      repository.getTradeClassDetails("XWM00000001770").futureValue

    result.mgdRegNumber         shouldBe "XWM00000001770"
    result.businessTradeClass   shouldBe None
    result.businessActivityDesc shouldBe ""
    result.systemDate           shouldBe None

    verify(tradeClassRs).close()
    verify(mockCs).close()
  }

  "getMgdDetails" should "return MgdDetails when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(mgdRs)
    when(mgdRs.next()).thenReturn(true)

    when(mgdRs.getString("mgd_reg_number")).thenReturn(mgdRegNumber)
    when(mgdRs.getObject("is_business_seasonal")).thenReturn(BigDecimal(1))

    when(mgdRs.getString("previous_mgdrn_1")).thenReturn("OLD001")
    when(mgdRs.getString("previous_mgdrn_2")).thenReturn("OLD002")
    when(mgdRs.getString("previous_mgdrn_3")).thenReturn("OLD003")

    when(mgdRs.getString("associated_mgdrn_1")).thenReturn("ASS001")
    when(mgdRs.getString("associated_mgdrn_2")).thenReturn("ASS002")
    when(mgdRs.getString("associated_mgdrn_3")).thenReturn("ASS003")

    when(mgdRs.getDate("system_date"))
      .thenReturn(Date.valueOf("2025-01-01"))

    val result =
      repository.getMgdDetails(mgdRegNumber).futureValue

    result shouldBe MgdDetails(
      mgdRegNumber       = mgdRegNumber,
      isBusinessSeasonal = Some(1),
      previousMgdrn1     = Some("OLD001"),
      previousMgdrn2     = Some("OLD002"),
      previousMgdrn3     = Some("OLD003"),
      associatedMgdrn1   = Some("ASS001"),
      associatedMgdrn2   = Some("ASS002"),
      associatedMgdrn3   = Some("ASS003"),
      systemDate         = Some(LocalDate.of(2025, 1, 1))
    )

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mgdRs).close()
    verify(mockCs).close()
  }

  it should "return default MgdDetails when cursor is null" in {

    when(mockCs.getObject(2)).thenReturn(null)

    val result =
      repository.getMgdDetails("XWM00000001770").futureValue

    result shouldBe MgdDetails(
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

    verify(mockCs).close()
  }

  it should "return default MgdDetails when result set is empty" in {

    when(mockCs.getObject(2)).thenReturn(mgdRs)
    when(mgdRs.next()).thenReturn(false)

    val result =
      repository.getMgdDetails("XWM00000001770").futureValue

    result shouldBe MgdDetails(
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

    verify(mgdRs).close()
    verify(mockCs).close()
  }

  it should "convert blank and null values to None" in {

    when(mockCs.getObject(2)).thenReturn(mgdRs)
    when(mgdRs.next()).thenReturn(true)

    when(mgdRs.getString("mgd_reg_number")).thenReturn("XWM00000001770")
    when(mgdRs.getObject("is_business_seasonal")).thenReturn(null)

    when(mgdRs.getString("previous_mgdrn_1")).thenReturn(" ")
    when(mgdRs.getString("previous_mgdrn_2")).thenReturn("")
    when(mgdRs.getString("previous_mgdrn_3")).thenReturn(null)

    when(mgdRs.getString("associated_mgdrn_1")).thenReturn(" ")
    when(mgdRs.getString("associated_mgdrn_2")).thenReturn("")
    when(mgdRs.getString("associated_mgdrn_3")).thenReturn(null)

    when(mgdRs.getDate("system_date")).thenReturn(null)

    val result =
      repository.getMgdDetails("XWM00000001770").futureValue

    result.mgdRegNumber       shouldBe "XWM00000001770"
    result.isBusinessSeasonal shouldBe None
    result.previousMgdrn1     shouldBe None
    result.previousMgdrn2     shouldBe None
    result.previousMgdrn3     shouldBe None
    result.associatedMgdrn1   shouldBe None
    result.associatedMgdrn2   shouldBe None
    result.associatedMgdrn3   shouldBe None
    result.systemDate         shouldBe None

    verify(mgdRs).close()
    verify(mockCs).close()
  }

  "getBusinessContactDetails" should "return BusinessContactDetails when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(businessContactRs)
    when(businessContactRs.next()).thenReturn(true)

    when(businessContactRs.getString("mgd_reg_number")).thenReturn(mgdRegNumber)
    when(businessContactRs.getString("phone_number")).thenReturn("111111")
    when(businessContactRs.getString("mobile_phone_number")).thenReturn("222222")
    when(businessContactRs.getString("fax_number")).thenReturn("333333")
    when(businessContactRs.getString("email_addr")).thenReturn("test@test.com")
    when(businessContactRs.getDate("system_date"))
      .thenReturn(Date.valueOf("2025-01-01"))

    val result =
      repository.getBusinessContactDetails(mgdRegNumber).futureValue

    result shouldBe BusinessContactDetails(
      mgdRegNumber      = mgdRegNumber,
      phoneNumber       = Some("111111"),
      mobilePhoneNumber = Some("222222"),
      faxNumber         = Some("333333"),
      emailAddr         = Some("test@test.com"),
      systemDate        = Some(LocalDate.of(2025, 1, 1))
    )

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(businessContactRs).close()
    verify(mockCs).close()
  }

  "getCorrespondenceDetails" should "return CorrespondenceDetails when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(correspondenceRs)
    when(correspondenceRs.next()).thenReturn(true)

    when(correspondenceRs.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(correspondenceRs.getString("NAME_LINE1")).thenReturn("foo")
    when(correspondenceRs.getString("NAME_LINE2")).thenReturn("foo")
    when(correspondenceRs.getString("PHONE_NUMBER")).thenReturn("07618728019")
    when(correspondenceRs.getString("MOBILE_PHONE_NUMBER")).thenReturn("018937617281")
    when(correspondenceRs.getString("FAX_NUMBER")).thenReturn("foo")
    when(correspondenceRs.getString("EMAIL_ADDR")).thenReturn("foo@mail.com")
    when(correspondenceRs.getString("ADI")).thenReturn("none")
    when(correspondenceRs.getString("ADDRESS_1")).thenReturn("random street")
    when(correspondenceRs.getString("ADDRESS_2")).thenReturn("bar")
    when(correspondenceRs.getString("ADDRESS_3")).thenReturn("bar")
    when(correspondenceRs.getString("ADDRESS_4")).thenReturn("bar")
    when(correspondenceRs.getString("POSTCODE")).thenReturn("SR1 4DE")
    when(correspondenceRs.getString("COUNTRY")).thenReturn("Ingerland!")
    when(correspondenceRs.getString("IOM_OR_CI_FLAG")).thenReturn("true")
    when(correspondenceRs.getDate("SYS_DATE")).thenReturn(Date.valueOf("2026-05-13"))

    val result =
      repository.getCorrespondenceDetails(mgdRegNumber).futureValue

    result shouldBe CorrespondenceDetails(
      mgdRegNumber      = "XWM00000001770",
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
      systemDate        = Some(LocalDate.of(2026, 5, 13))
    )

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(correspondenceRs).close()
    verify(mockCs).close()
  }

  "getBusinessAddressDetails" should "return BusinessAddressDetails when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(businessAddressRs)
    when(businessAddressRs.next()).thenReturn(true)

    when(businessAddressRs.getString("mgd_reg_number")).thenReturn(mgdRegNumber)
    when(businessAddressRs.getString("adi")).thenReturn("none")
    when(businessAddressRs.getString("address_1")).thenReturn("random street")
    when(businessAddressRs.getString("address_2")).thenReturn("bar")
    when(businessAddressRs.getString("address_3")).thenReturn("bar")
    when(businessAddressRs.getString("address_4")).thenReturn("bar")
    when(businessAddressRs.getString("postcode")).thenReturn("SR1 4DE")
    when(businessAddressRs.getString("country")).thenReturn("Ingerland!")
    when(businessAddressRs.getString("iom_or_ci_flag")).thenReturn("true")
    when(businessAddressRs.getDate("system_date")).thenReturn(Date.valueOf("2026-05-13"))

    val result =
      repository.getBusinessAddressDetails(mgdRegNumber).futureValue

    result shouldBe BusinessAddressDetails(
      mgdRegNumber = "XWM00000001770",
      adi          = Some("none"),
      address1     = Some("random street"),
      address2     = Some("bar"),
      address3     = Some("bar"),
      address4     = Some("bar"),
      postcode     = Some("SR1 4DE"),
      country      = Some("Ingerland!"),
      iomOrCiFlag  = Some("true"),
      systemDate   = Some(LocalDate.of(2026, 5, 13))
    )

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(businessAddressRs).close()
    verify(mockCs).close()
  }

  "getPremisesDetails" should "return PremisesDetails when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(4)).thenReturn(premisesRs)
    when(premisesRs.next()).thenReturn(true, false)

    when(premisesRs.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(premisesRs.getString("ADDRESS_1")).thenReturn("random street")
    when(premisesRs.getString("ADDRESS_2")).thenReturn("bar")
    when(premisesRs.getString("ADDRESS_3")).thenReturn("bar")
    when(premisesRs.getString("ADDRESS_4")).thenReturn("bar")
    when(premisesRs.getString("POSTCODE")).thenReturn("SR1 4DE")
    when(premisesRs.getDate("SYSTEM_DATE")).thenReturn(Date.valueOf("2026-05-13"))
    when(mockCs.getObject(5)).thenReturn(java.math.BigDecimal.valueOf(100))

    val result =
      repository.getPremisesDetails(mgdRegNumber).futureValue

    result shouldBe PremisesDetailsResponse(
      totalRows = Some(100),
      premises = Seq(
        PremisesDetails(
          mgdRegNumber = "XWM00000001770",
          address1     = Some("random street"),
          address2     = Some("bar"),
          address3     = Some("bar"),
          address4     = Some("bar"),
          postcode     = Some("SR1 4DE"),
          systemDate   = Some(LocalDate.of(2026, 5, 13))
        )
      )
    )

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).setInt(2, -1)
    verify(mockCs).setInt(3, 0)
    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(5, java.sql.Types.NUMERIC)
    verify(mockCs).execute()
    verify(premisesRs).close()
    verify(mockCs).close()
  }

  "getOperatorDetails" should "return operator details when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(operatorRs)

    when(operatorRs.next()).thenReturn(true)

    when(operatorRs.getString("mgd_reg_number")).thenReturn(mgdRegNumber)

    when(operatorRs.getString("sole_prop_name")).thenReturn("John Trading")
    when(operatorRs.getString("business_name")).thenReturn("Test Business")
    when(operatorRs.getString("trading_name")).thenReturn("Trading Ltd")
    when(operatorRs.getObject("business_type")).thenReturn(BigDecimal(2))

    when(operatorRs.getString("address_1")).thenReturn("Line 1")
    when(operatorRs.getString("postcode")).thenReturn("AB12 3CD")

    val result = repository.getOperatorDetails(mgdRegNumber).futureValue

    result.mgdRegNumber shouldBe mgdRegNumber
    result.solePropName shouldBe Some("John Trading")
    result.businessName shouldBe Some("Test Business")
    result.tradingName  shouldBe Some("Trading Ltd")
    result.businessType shouldBe Some(2)
    result.address1     shouldBe Some("Line 1")
    result.postcode     shouldBe Some("AB12 3CD")

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
  }

  it should "throw exception when operator cursor is empty" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(operatorRs)
    when(operatorRs.next()).thenReturn(false)

    val ex = intercept[RuntimeException] {
      repository.getOperatorDetails(mgdRegNumber).futureValue
    }

    ex.getMessage should include("No data")

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
  }

  it should "throw exception when operator cursor is null" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(null)

    val ex = intercept[RuntimeException] {
      repository.getOperatorDetails(mgdRegNumber).futureValue
    }

    ex.getMessage should include("Null cursor")

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
  }

  "getBusinessDetails" should "return BusinessDetails when data exists" in {

    val mgdRegNumber = "XWM00000001770"

    when(mockCs.getObject(2)).thenReturn(businessRs)

    when(businessRs.next()).thenReturn(true)

    when(businessRs.getString("mgd_reg_number")).thenReturn(mgdRegNumber)
    when(businessRs.getObject("business_type")).thenReturn(BigDecimal(1))
    when(businessRs.getInt("currently_registered")).thenReturn(1)

    when(businessRs.getInt("group_reg")).thenReturn(1)
    when(businessRs.getDate("date_of_registration"))
      .thenReturn(Date.valueOf("2020-01-01"))

    when(businessRs.getString("business_partner_number"))
      .thenReturn("BP123")

    val result = repository.getBusinessDetails(mgdRegNumber).futureValue

    result.mgdRegNumber          shouldBe mgdRegNumber
    result.currentlyRegistered   shouldBe 1
    result.groupReg              shouldBe true
    result.businessPartnerNumber shouldBe Some("BP123")

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
  }

  it should "throw exception when business details cursor is empty" in {

    when(mockCs.getObject(2)).thenReturn(businessRs)
    when(businessRs.next()).thenReturn(false)

    val ex = intercept[RuntimeException] {
      repository.getBusinessDetails("XWM00000001770").futureValue
    }

    ex.getMessage should include("No business details found")

    verify(mockCs).setString(1, "XWM00000001770")
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
  }

  it should "throw exception when business cursor is null" in {

    when(mockCs.getObject(2)).thenReturn(null)

    val ex = intercept[RuntimeException] {
      repository.getBusinessDetails("XWM00000001770").futureValue
    }

    ex.getMessage should include("No business details found")

    verify(mockCs).setString(1, "XWM00000001770")
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
  }

  it should "throw exception when operator cursor is empty" in {

    when(mockCs.getObject(2)).thenReturn(operatorRs)
    when(operatorRs.next()).thenReturn(false)

    val ex = intercept[RuntimeException] {
      repository.getOperatorDetails("XWM00000001770").futureValue
    }

    ex.getMessage should include("No data")

    verify(operatorRs).close()
    verify(mockCs).close()
  }

  it should "throw exception when operator cursor is null" in {

    when(mockCs.getObject(2)).thenReturn(null)

    val ex = intercept[RuntimeException] {
      repository.getOperatorDetails("XWM00000001770").futureValue
    }

    ex.getMessage should include("Null cursor")

    verify(mockCs).close()
  }

  "getReturnSummary" should "return ReturnSummary when stored procedure returns data" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockCs.getObject(2)).thenReturn(returnSummaryRs)
    when(returnSummaryRs.next()).thenReturn(true)
    when(returnSummaryRs.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(returnSummaryRs.getInt("RETURNS_DUE")).thenReturn(5)
    when(returnSummaryRs.getInt("RETURNS_OVERDUE")).thenReturn(2)

    val result = repository.getReturnSummary(mgdRegNumber).futureValue

    result shouldBe ReturnSummary(mgdRegNumber, 5, 2)

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(returnSummaryRs).close()
    verify(mockCs).close()
  }

  it should "throw exception when result set is empty" in {

    when(mockCs.getObject(2)).thenReturn(returnSummaryRs)
    when(returnSummaryRs.next()).thenReturn(false)

    val ex = intercept[RuntimeException] {
      repository.getReturnSummary("XWM123").futureValue
    }

    ex.getMessage should include("Empty result set")
    verify(returnSummaryRs).close()
    verify(mockCs).close()
  }

  it should "throw exception when cursor is null" in {

    when(mockCs.getObject(2)).thenReturn(null)

    val ex = intercept[RuntimeException] {
      repository.getReturnSummary("XWM123").futureValue
    }

    ex.getMessage should include("Null cursor")
    verify(mockCs).close()
  }

  "getMgdCertificate" should "return full MgdCertificate when all data is present" in {

    val mgdRegNumber = "XWM00000001770"
    val issuedDate = LocalDate.of(2024, 1, 1)

    // Scalar outs
    when(mockCs.getString(2)).thenReturn(mgdRegNumber)
    when(mockCs.getDate(3)).thenReturn(Date.valueOf("2020-01-01"))
    when(mockCs.getString(5)).thenReturn("Test Business")
    when(mockCs.getString(25)).thenReturn("Y")
    when(mockCs.getObject(26)).thenReturn(BigDecimal(2))
    when(mockCs.getDate(28)).thenReturn(Date.valueOf(issuedDate))

    // Cursors
    when(mockCs.getObject(24)).thenReturn(partRs)
    when(mockCs.getObject(27)).thenReturn(groupRs)
    when(mockCs.getObject(29)).thenReturn(returnPeriodRs)

    // Partner cursor
    when(partRs.next()).thenReturn(true, false)
    when(partRs.getString("names_of_part_mems")).thenReturn("Partner 1")
    when(partRs.getInt("type_of_business")).thenReturn(1)

    // Group cursor
    when(groupRs.next()).thenReturn(true, false)
    when(groupRs.getString("names_of_group_mems")).thenReturn("Group 1")

    // Return periods cursor
    when(returnPeriodRs.next()).thenReturn(true, false)
    when(returnPeriodRs.getDate("return_period_end_date"))
      .thenReturn(Date.valueOf("2023-12-31"))

    val result = repository.getMgdCertificate(mgdRegNumber).futureValue

    result.mgdRegNumber   shouldBe mgdRegNumber
    result.businessName   shouldBe Some("Test Business")
    result.groupReg       shouldBe "Y"
    result.noOfGroupMems  shouldBe Some(2)
    result.dateCertIssued shouldBe Some(issuedDate)

    result.partMembers.size          shouldBe 1
    result.groupMembers.size         shouldBe 1
    result.returnPeriodEndDates.size shouldBe 1

    verify(partRs).close()
    verify(groupRs).close()
    verify(returnPeriodRs).close()
    verify(mockCs).close()
  }

  it should "return empty lists when cursors are empty" in {

    when(mockCs.getObject(24)).thenReturn(partRs)
    when(mockCs.getObject(27)).thenReturn(groupRs)
    when(mockCs.getObject(29)).thenReturn(returnPeriodRs)

    when(partRs.next()).thenReturn(false)
    when(groupRs.next()).thenReturn(false)
    when(returnPeriodRs.next()).thenReturn(false)

    val result = repository.getMgdCertificate("XWM00000001770").futureValue

    result.partMembers          shouldBe empty
    result.groupMembers         shouldBe empty
    result.returnPeriodEndDates shouldBe empty

    verify(partRs).close()
    verify(groupRs).close()
    verify(returnPeriodRs).close()
    verify(mockCs).close()
  }

  it should "handle null cursors safely" in {

    when(mockCs.getObject(24)).thenReturn(null)
    when(mockCs.getObject(27)).thenReturn(null)
    when(mockCs.getObject(29)).thenReturn(null)

    val result = repository.getMgdCertificate("XWM00000001770").futureValue

    result.partMembers          shouldBe empty
    result.groupMembers         shouldBe empty
    result.returnPeriodEndDates shouldBe empty

    verify(mockCs).close()
  }

  it should "close resources when execute throws exception" in {

    when(mockCs.execute()).thenThrow(new RuntimeException("DB error"))

    val ex = repository.getMgdCertificate("XWM00000001770").failed.futureValue
    ex.getMessage should include("DB error")

    verify(mockCs).close()
  }

  "getAgentDetails" should "return Right(AgentDetails) when name, address and contact are all present" in {

    val agentReference = "AGENT001"

    when(mockCs.getString(2)).thenReturn("Gambling company 1")

    when(mockCs.getObject(3)).thenReturn(agentAddressRs)
    when(agentAddressRs.next()).thenReturn(true)
    when(agentAddressRs.getString("ADDR_1")).thenReturn("1")
    when(agentAddressRs.getString("ADDR_2")).thenReturn("Example street")
    when(agentAddressRs.getString("ADDR_3")).thenReturn("Town")
    when(agentAddressRs.getString("ADDR_4")).thenReturn("County")
    when(agentAddressRs.getString("POST_CODE")).thenReturn("SW1A 1AA")
    when(agentAddressRs.getString("COUNTRY")).thenReturn("United Kingdom")
    when(agentAddressRs.getString("ABROAD_SIGNAL")).thenReturn("N")

    when(mockCs.getObject(4)).thenReturn(agentContactRs)
    when(agentContactRs.next()).thenReturn(true)
    when(agentContactRs.getString("PHONE_NUMBER")).thenReturn("02079460000")
    when(agentContactRs.getString("MOBILE_PHONE_NUMBER")).thenReturn("07700900999")
    when(agentContactRs.getString("FAX_NUMBER")).thenReturn("02079460123")
    when(agentContactRs.getString("EMAIL_ADDR")).thenReturn("user@example.com")

    val result = repository.getAgentDetails(agentReference).futureValue

    result shouldBe Right(
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

    verify(mockCs).setString(1, agentReference)
    verify(mockCs).registerOutParameter(2, java.sql.Types.VARCHAR)
    verify(mockCs).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(agentAddressRs).close()
    verify(agentContactRs).close()
    verify(mockCs).close()
  }

  it should "return Left(RecordNotFound) when the address cursor is empty" in {

    when(mockCs.getObject(3)).thenReturn(agentAddressRs)
    when(agentAddressRs.next()).thenReturn(false)

    val result = repository.getAgentDetails("UNKNOWN").futureValue

    result shouldBe Left(RecordNotFound("No agent details found for agentReference=UNKNOWN"))

    verify(agentAddressRs).close()
    verify(mockCs).close()
  }

  it should "return Left(RecordNotFound) when the address cursor is null" in {

    when(mockCs.getObject(3)).thenReturn(null)

    val result = repository.getAgentDetails("UNKNOWN").futureValue

    result shouldBe Left(RecordNotFound("No agent details found for agentReference=UNKNOWN"))

    verify(mockCs).close()
  }

  it should "trim values and treat blank strings as None" in {

    when(mockCs.getString(2)).thenReturn("  Gambling company 1  ")
    when(mockCs.getObject(3)).thenReturn(agentAddressRs)
    when(agentAddressRs.next()).thenReturn(true)
    when(agentAddressRs.getString("ADDR_1")).thenReturn(" 1 ")
    when(agentAddressRs.getString("ADDR_2")).thenReturn("")
    when(agentAddressRs.getString("ADDR_3")).thenReturn(null)
    when(agentAddressRs.getString("ADDR_4")).thenReturn(null)
    when(agentAddressRs.getString("POST_CODE")).thenReturn(null)
    when(agentAddressRs.getString("COUNTRY")).thenReturn(null)
    when(agentAddressRs.getString("ABROAD_SIGNAL")).thenReturn(null)
    when(mockCs.getObject(4)).thenReturn(agentContactRs)
    when(agentContactRs.next()).thenReturn(true)

    val result = repository.getAgentDetails("AGENT001").futureValue.toOption.get

    result.businessName shouldBe Some("Gambling company 1")
    result.addressLine1 shouldBe Some("1")
    result.addressLine2 shouldBe None
    result.addressLine3 shouldBe None
  }

  it should "return Left(RecordNotFound) and still close resources when execute throws ORA-01403" in {

    when(mockCs.execute()).thenThrow(new java.sql.SQLException("ORA-01403: no data found", "02000", 1403))

    val result = repository.getAgentDetails("UNKNOWN").futureValue

    result match {
      case Left(RecordNotFound(msg)) => msg should include("GET_AGENT_DETAILS")
      case other                     => fail(s"expected Left(RecordNotFound), got $other")
    }

    verify(mockCs).close()
  }

  it should "return Left(DatabaseError) and still close resources when execute throws another SQLException" in {

    when(mockCs.execute()).thenThrow(new java.sql.SQLException("ORA-00942: table or view does not exist", "42000", 942))

    val result = repository.getAgentDetails("AGENT001").futureValue

    result match {
      case Left(DatabaseError(msg, _)) => msg should include("GET_AGENT_DETAILS")
      case other                       => fail(s"expected Left(DatabaseError), got $other")
    }

    verify(mockCs).close()
  }

  "getGroupMembers" should "return GroupMembers when data exists" in {

    val mgdRegNumber = "XWM12345678901"
    when(mockCs.getObject(2)).thenReturn(groupMembersRs)
    when(mockCs.getObject(3)).thenReturn(Date.valueOf("1991-01-03"))

    when(groupMembersRs.next()).thenReturn(true, false)

    when(groupMembersRs.getString("mgd_reg_number")).thenReturn(mgdRegNumber)
    when(groupMembersRs.getString("BUSINESS_PARTNER_NUMBER")).thenReturn("3289084908")
    when(groupMembersRs.getString("BUSINESS_NAME")).thenReturn("abc")
    when(groupMembersRs.getString("CRN")).thenReturn("123455")
    when(groupMembersRs.getString("FOREIGN_CORPORATE_REF")).thenReturn("123455")
    when(groupMembersRs.getInt("IS_REP_MEMBER_OF_GROUP")).thenReturn(1)
    when(groupMembersRs.getDate("DATE_OF_JOINING"))
      .thenReturn(Date.valueOf("1991-01-01"))
    when(groupMembersRs.getDate("DATE_OF_LEAVING"))
      .thenReturn(Date.valueOf("1991-01-02"))
    when(groupMembersRs.getString("EMAIL_ADDR")).thenReturn("a@b.com")

    val result = repository.getGroupMembers(mgdRegNumber).futureValue
    result.groupMembers.head.mgdRegNumber          shouldBe mgdRegNumber
    result.groupMembers.head.businessPartnerNumber shouldBe Some("3289084908")
    result.groupMembers.head.businessName          shouldBe Some("abc")
    result.groupMembers.head.crn                   shouldBe Some("123455")
    result.groupMembers.head.isRepMemberOfGroup    shouldBe Some(1)
    result.groupMembers.head.dateOfJoining         shouldBe Some(LocalDate.of(1991, 1, 1))
    result.groupMembers.head.dateOfLeaving         shouldBe Some(LocalDate.of(1991, 1, 2))
    result.groupMembers.head.emailAddr             shouldBe Some("a@b.com")

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    mockCs.getObject(3) shouldBe Date.valueOf("1991-01-03")
    verify(mockCs).registerOutParameter(3, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).execute()
  }
  "getMemberDetails" should "return Member Details when data exists" in {
    val mgdRegNumber = "XWM12345678901"
    val partnerNumber = "0100049988"
    val long: Long = 1234567890
    val premises = Seq(
      PremisesDetails(
        mgdRegNumber = "XWM12345678901",
        address1     = Some("123 Road"),
        address2     = Some("Place"),
        address3     = Some("More info about this place"),
        address4     = Some("Even more? noo"),
        postcode     = Some("SA0 1AH"),
        systemDate   = Some(LocalDate.of(2026, 11, 16))
      )
    )

    when(mockCs.getObject(3)).thenReturn(premisesRs) // P_PREMISES
    when(mockCs.getString(4)).thenReturn(mgdRegNumber) // MGD_REG_NUMBER
    when(mockCs.getString(5)).thenReturn(partnerNumber) // BUSINESS_PARTNER_NUMBER
    when(mockCs.getString(6)).thenReturn("Business Name") // BUSINESS_NAME
    when(mockCs.getString(7)).thenReturn("Trading Name") // TRADING_NAME
    when(mockCs.getString(8)).thenReturn("09876543") // CRN
    when(mockCs.getDate(9)).thenReturn(Date.valueOf("2025-01-01")) // DATE_OF_INCORPORATION
    when(mockCs.getString(10)).thenReturn("UK") // COUNTRY_OF_INCORPORATION
    when(mockCs.getString(11)).thenReturn("FCR-UK-987654") // FOREIGN_CORPORATE_REF
    when(mockCs.getLong(12)).thenReturn(long) // UTR
    when(mockCs.getLong(13)).thenReturn(long) // VRN
    when(mockCs.getInt(14)).thenReturn(3) // TRADE_CLASS
    when(mockCs.getString(15)).thenReturn("Business Activities") // BUS_ACT_DESCRIPTION
    when(mockCs.getString(16)).thenReturn("0800 123 456") // PHONE_NUMBER
    when(mockCs.getString(17)).thenReturn("07900 678 900") // MOBILE_PHONE_NUMBER
    when(mockCs.getString(18)).thenReturn("No one uses fax anymore") // FAX_NUMBER
    when(mockCs.getString(19)).thenReturn("a@b.com") // EMAIL_ADDR
    when(mockCs.getString(20)).thenReturn("our friend adi") // ADI
    when(mockCs.getString(21)).thenReturn("123 Road") // ADDRESS_1
    when(mockCs.getString(22)).thenReturn("Place") // ADDRESS_2
    when(mockCs.getString(23)).thenReturn("More info about this place") // ADDRESS_3
    when(mockCs.getString(24)).thenReturn("Even more? noo") // ADDRESS_4
    when(mockCs.getString(25)).thenReturn("SA0 1AH") // POSTCODE
    when(mockCs.getString(26)).thenReturn("1") // IS_IOM_OR_CI
    when(mockCs.getString(27)).thenReturn("0") // HAVE_GAMBLING_LICENCE_NO
    when(mockCs.getString(28)).thenReturn("123-456789-A-123456-789") // GAMBLING_LICENCE_NO
    when(mockCs.getString(29)).thenReturn("0") // HELD_BY_LANDLORD
    when(mockCs.getString(30)).thenReturn("1") // LOCAL_AUTHORITY
    when(mockCs.getString(31)).thenReturn("0") // FAMILY_ENTERTAINMENT
    when(mockCs.getString(32)).thenReturn("1") // CLUB_GAMING
    when(mockCs.getString(33)).thenReturn("0") // CLUB_LICENCE
    when(mockCs.getString(34)).thenReturn("1") // PRIZE_GAMING
    when(mockCs.getString(35)).thenReturn("0") // ON_PREMISES
    when(mockCs.getString(36)).thenReturn("1") // CLUB_PREMISES
    when(mockCs.getString(37)).thenReturn("0") // REG_CERT
    when(mockCs.getString(38)).thenReturn("1") // BOOKMAKING
    when(mockCs.getString(39)).thenReturn("0") // BINGO
    when(mockCs.getString(40)).thenReturn("1") // AMUSEMENT
    when(mockCs.getString(41)).thenReturn("0") // SERVE_ALCOHOL
    when(mockCs.getString(42)).thenReturn("1") // PREMISES_NOT_COVERED
    when(mockCs.getString(43)).thenReturn("XVM12345678800") // PREVIOUS_MGDRN_1
    when(mockCs.getString(44)).thenReturn("XVM12345678801") // PREVIOUS_MGDRN_2
    when(mockCs.getString(45)).thenReturn("XVM12345678802") // PREVIOUS_MGDRN_3
    when(mockCs.getString(46)).thenReturn("XGM12345678800") // ASSOCIATED_MGDRN_1
    when(mockCs.getString(47)).thenReturn("XGM12345678801") // ASSOCIATED_MGDRN_2
    when(mockCs.getString(48)).thenReturn("XGM123456788002") // ASSOCIATED_MGDRN_3
    when(mockCs.getDate(49)).thenReturn(Date.valueOf("2026-01-16")) // DATE_OF_JOINING
    when(mockCs.getDate(50)).thenReturn(Date.valueOf("2026-10-16")) // DATE_OF_LEAVING
    when(mockCs.getString(51)).thenReturn("1") // IS_REP_MEM_SAME_AS_CB
    when(mockCs.getString(52)).thenReturn("1") // IS_REP_MEMBER_OF_GROUP
    when(mockCs.getString(53)).thenReturn("1") // IS_UK_INCORPORATED
    when(mockCs.getDate(54)).thenReturn(Date.valueOf("2026-11-16")) // P_SYSDATE

    when(premisesRs.next()).thenReturn(true, false)

    when(premisesRs.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(premisesRs.getString("ADDRESS_1")).thenReturn("123 Road")
    when(premisesRs.getString("ADDRESS_2")).thenReturn("Place")
    when(premisesRs.getString("ADDRESS_3")).thenReturn("More info about this place")
    when(premisesRs.getString("ADDRESS_4")).thenReturn("Even more? noo")
    when(premisesRs.getString("POSTCODE")).thenReturn("SA0 1AH")
    when(premisesRs.getDate("SYSTEM_DATE"))
      .thenReturn(Date.valueOf("2026-11-16"))

    val result: Either[RepositoryError, MemberDetails] = repository.getMemberDetails(Regime.MGD, mgdRegNumber, partnerNumber).futureValue
    println(result)
    result.map(_.mgdRegNumber).getOrElse("")           shouldBe mgdRegNumber
    result.map(_.businessPartnerNumber).getOrElse("")  shouldBe partnerNumber
    result.map(_.businessName).getOrElse("")           shouldBe Some("Business Name")
    result.map(_.tradingName).getOrElse("")            shouldBe Some("Trading Name")
    result.map(_.crn).getOrElse("")                    shouldBe Some("09876543")
    result.map(_.dateOfIncorporation).getOrElse("")    shouldBe Some(LocalDate.of(2025, 1, 1))
    result.map(_.countryOfIncorporation).getOrElse("") shouldBe Some("UK")
    result.map(_.foreignCorporateRef).getOrElse("")    shouldBe Some("FCR-UK-987654")
    result.map(_.utr).getOrElse(999)                   shouldBe Some(long)
    result.map(_.vrn).getOrElse(999)                   shouldBe Some(long)
    result.map(_.tradeClass).getOrElse(999)            shouldBe Some(3)
    result.map(_.businessActivityDesc).getOrElse("")   shouldBe Some("Business Activities")
    result.map(_.phoneNumber).getOrElse("")            shouldBe Some("0800 123 456")
    result.map(_.mobilePhoneNumber).getOrElse("")      shouldBe Some("07900 678 900")
    result.map(_.faxNumber).getOrElse("")              shouldBe Some("No one uses fax anymore")
    result.map(_.emailAddr).getOrElse("")              shouldBe Some("a@b.com")
    result.map(_.adi).getOrElse("")                    shouldBe Some("our friend adi")
    result.map(_.address1).getOrElse("")               shouldBe Some("123 Road")
    result.map(_.address2).getOrElse("")               shouldBe Some("Place")
    result.map(_.address3).getOrElse("")               shouldBe Some("More info about this place")
    result.map(_.address4).getOrElse("")               shouldBe Some("Even more? noo")
    result.map(_.postcode).getOrElse("")               shouldBe Some("SA0 1AH")
    result.map(_.isIomOrCiFlag).getOrElse("")          shouldBe Some("1")
    result.map(_.haveGamblingLicenceNo).getOrElse("")  shouldBe Some("0")
    result.map(_.gamblingLicenceNo).getOrElse("")      shouldBe Some("123-456789-A-123456-789")
    result.map(_.heldByLandlord).getOrElse("")         shouldBe Some("0")
    result.map(_.localAuthority).getOrElse("")         shouldBe Some("1")
    result.map(_.familyEntertainment).getOrElse("")    shouldBe Some("0")
    result.map(_.clubGaming).getOrElse("")             shouldBe Some("1")
    result.map(_.clubLicence).getOrElse("")            shouldBe Some("0")
    result.map(_.prizeGaming).getOrElse("")            shouldBe Some("1")
    result.map(_.onPremises).getOrElse("")             shouldBe Some("0")
    result.map(_.clubPremises).getOrElse("")           shouldBe Some("1")
    result.map(_.regCert).getOrElse("")                shouldBe Some("0")
    result.map(_.bookmaking).getOrElse("")             shouldBe Some("1")
    result.map(_.bingo).getOrElse("")                  shouldBe Some("0")
    result.map(_.amusement).getOrElse("")              shouldBe Some("1")
    result.map(_.serveAlcohol).getOrElse("")           shouldBe Some("0")
    result.map(_.premisesNotCovered).getOrElse("")     shouldBe Some("1")
    result.map(_.previousMgdrn1).getOrElse("")         shouldBe Some("XVM12345678800")
    result.map(_.previousMgdrn2).getOrElse("")         shouldBe Some("XVM12345678801")
    result.map(_.previousMgdrn3).getOrElse("")         shouldBe Some("XVM12345678802")
    result.map(_.associatedMgdrn1).getOrElse("")       shouldBe Some("XGM12345678800")
    result.map(_.associatedMgdrn2).getOrElse("")       shouldBe Some("XGM12345678801")
    result.map(_.associatedMgdrn3).getOrElse("")       shouldBe Some("XGM123456788002")
    result.map(_.dateOfJoining).getOrElse("")          shouldBe Some(LocalDate.of(2026, 1, 16))
    result.map(_.dateOfLeaving).getOrElse("")          shouldBe Some(LocalDate.of(2026, 10, 16))
    result.map(_.isRepMemSameAsCb).getOrElse("")       shouldBe Some("1")
    result.map(_.isRepMemberOfGroup).getOrElse("")     shouldBe Some("1")
    result.map(_.isUkIncorporated).getOrElse("")       shouldBe Some("1")
    result.map(_.systemDate).getOrElse("")             shouldBe Some(LocalDate.of(2026, 11, 16))
    result.map(_.premises).getOrElse(Seq.empty)        shouldBe premises

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).setString(2, partnerNumber)
    verify(mockCs).registerOutParameter(3, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(4, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(5, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(7, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(8, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(9, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(10, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(11, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(12, oracle.jdbc.OracleTypes.NUMERIC)
    verify(mockCs).registerOutParameter(13, oracle.jdbc.OracleTypes.NUMERIC)
    verify(mockCs).registerOutParameter(14, oracle.jdbc.OracleTypes.NUMBER)
    verify(mockCs).registerOutParameter(15, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(16, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(17, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(18, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(19, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(20, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(21, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(22, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(23, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(24, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(25, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(26, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(27, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(28, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(29, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(30, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(31, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(32, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(33, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(34, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(35, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(36, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(37, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(38, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(39, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(40, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(41, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(42, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(43, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(44, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(45, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(46, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(47, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(48, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(49, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(50, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).registerOutParameter(51, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(52, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(53, oracle.jdbc.OracleTypes.VARCHAR)
    verify(mockCs).registerOutParameter(54, oracle.jdbc.OracleTypes.DATE)
    verify(mockCs).execute()
  }

  it should "return DBError when partner number is incorrect" in {

    val mgdRegNumber = "XWM12345678901"
    val partnerNumber = "INVALID"

    when(mockCs.getObject(3)).thenReturn(Seq.empty) // MGD_REG_NUMBER
    when(mockCs.getString(4)).thenReturn(mgdRegNumber) // MGD_REG_NUMBER
    when(mockCs.getString(5)).thenReturn(partnerNumber) // BUSINESS_PARTNER_NUMBER
    when(premisesRs.next()).thenReturn(false)

    val result = repository.getMemberDetails(Regime.MGD, mgdRegNumber, partnerNumber).futureValue

    result.map(_.premises).getOrElse(Seq.empty) shouldBe empty

    result.left.value shouldBe a[DatabaseError]

    verify(mockCs).setString(1, mgdRegNumber)
    verify(mockCs).setString(2, partnerNumber)
    verify(mockCs).close()
  }

}
