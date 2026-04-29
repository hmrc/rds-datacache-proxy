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

  before {
    db              = mock(classOf[Database])
    mockConnection  = mock(classOf[Connection])
    mockCs          = mock(classOf[CallableStatement])
    returnSummaryRs = mock(classOf[ResultSet])
    partRs          = mock(classOf[ResultSet])
    groupRs         = mock(classOf[ResultSet])
    returnPeriodRs  = mock(classOf[ResultSet])
    businessRs      = mock(classOf[ResultSet])
    operatorRs      = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCs)

    repository = new GamblingDataCacheRepository(db)
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
    when(businessRs.getString("group_reg")).thenReturn("Y")
    when(businessRs.getDate("date_of_registration")).thenReturn(Date.valueOf("2020-01-01"))
    when(businessRs.getString("business_partner_number")).thenReturn("BP123")

    val result = repository.getBusinessDetails(mgdRegNumber).futureValue

    result.mgdRegNumber          shouldBe mgdRegNumber
    result.currentlyRegistered   shouldBe 1
    result.isGroupMember         shouldBe true
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
