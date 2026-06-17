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
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingDataCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

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

}
