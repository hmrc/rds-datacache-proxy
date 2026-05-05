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
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseReturnsSubmittedSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
//import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingReturnsDataCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: GamblingReturnsDataCacheRepository = _
  var mockConnection: Connection = _
  var mockCs: CallableStatement = _
  var amountDeclaredRs: ResultSet = _

  before {
    db               = mock(classOf[Database])
    mockConnection   = mock(classOf[Connection])
    mockCs           = mock(classOf[CallableStatement])
    amountDeclaredRs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCs)

    repository = new GamblingReturnsDataCacheRepository(db)
  }

  "getReturnsSubmitted" should "return ReturnsSubmitted when stored procedure returns data" in {

    val regNumber = "XWM12345678901"

    when(mockCs.getDate(2)).thenReturn(Date.valueOf("2016-2-29"))
    when(mockCs.getDate(3)).thenReturn(Date.valueOf("2017-6-15"))
    when(mockCs.getObject(4)).thenReturn(BigDecimal.valueOf(-301.56))
    when(mockCs.getObject(5)).thenReturn(1)
    when(mockCs.getObject(6)).thenReturn(amountDeclaredRs)
    when(amountDeclaredRs.next()).thenReturn(true, false)

    when(amountDeclaredRs.getInt("p_desc_code")).thenReturn(4455)
    when(amountDeclaredRs.getDate("p_period_start")).thenReturn(Date.valueOf("2016-3-09"))
    when(amountDeclaredRs.getDate("p_period_end")).thenReturn(Date.valueOf("2016-5-20"))
    when(amountDeclaredRs.getObject("p_amount")).thenReturn(BigDecimal.valueOf(-943.21))

    val result = repository.getReturnsSubmitted(regNumber, 1, 10).futureValue

    result shouldBe validResponseReturnsSubmittedSmall

    verify(mockCs).setString(1, regNumber)
    verify(mockCs).registerOutParameter(2, java.sql.Types.DATE)
    verify(mockCs).registerOutParameter(6, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(amountDeclaredRs).close()
    verify(mockCs).close()
  }

//  "getReturnsSubmitted" should "return None when regNumber is null" in {
//    val regNumber = "AGENT123"
//    when(mockCs.getDate(2)).thenReturn(null)
//    val result = repository.getReturnsSubmitted(regNumber, 1, 10).futureValue
//
//    System.out.println("JBS:2:" + result)
////    result shouldBe Null
//
//    verify(mockCs).setString(1, regNumber)
//    verify(mockCs).registerOutParameter(2, java.sql.Types.DATE)
//    verify(mockCs).execute()
//    verify(mockCs).getString("p_name")
//    verify(mockCs).close()
//  }

//  "getOrganisationName" should "return Some(organisation name) when organisation name exists" in {
//    val charityRef = "CHARITY123"
//    val expectedName = "Test Organisation Name"
//
//    when(mockCallableStatement.getString("p_name")).thenReturn(expectedName)
//
//    val result = repository.getOrganisationName(charityRef).futureValue
//
//    result shouldBe Some(expectedName)
//
//    verify(mockCallableStatement).setString("p_charity_ref", charityRef)
//    verify(mockCallableStatement).registerOutParameter("p_name", Types.VARCHAR)
//    verify(mockCallableStatement).execute()
//    verify(mockCallableStatement).getString("p_name")
//    verify(mockCallableStatement).close()
//  }
//  it should "throw exception when result set is empty" in {
//
//    when(mockCs.getObject(2)).thenReturn(returnsSubmittedRs)
//    when(returnsSubmittedRs.next()).thenReturn(false)
//
//    val ex = intercept[RuntimeException] {
//      repository.getReturnsSubmitted("XWM123").futureValue
//    }
//
//    ex.getMessage should include("Empty result set")
//    verify(returnsSubmittedRs).close()
//    verify(mockCs).close()
//  }
//
//  it should "throw exception when cursor is null" in {
//
//    when(mockCs.getObject(2)).thenReturn(null)
//
//    val ex = intercept[RuntimeException] {
//      repository.getReturnsSubmitted("XWM123").futureValue
//    }
//
//    ex.getMessage should include("Null cursor")
//    verify(mockCs).close()
//  }
//
//  "getMgdCertificate" should "return full MgdCertificate when all data is present" in {
//
//    val regNumber = "XWM00000001770"
//    val issuedDate = LocalDate.of(2024, 1, 1)
//
//    // Scalar outs
//    when(mockCs.getString(2)).thenReturn(regNumber)
//    when(mockCs.getDate(3)).thenReturn(Date.valueOf("2020-01-01"))
//    when(mockCs.getString(5)).thenReturn("Test Business")
//    when(mockCs.getString(25)).thenReturn("Y")
//    when(mockCs.getObject(26)).thenReturn(BigDecimal(2))
//    when(mockCs.getDate(28)).thenReturn(Date.valueOf(issuedDate))
//
//    // Cursors
//    when(mockCs.getObject(24)).thenReturn(amountDeclaredRs)
//    when(mockCs.getObject(27)).thenReturn(groupRs)
//    when(mockCs.getObject(29)).thenReturn(returnPeriodRs)
//
//    // Partner cursor
//    when(amountDeclaredRs.next()).thenReturn(true, false)
//    when(amountDeclaredRs.getString("names_of_part_mems")).thenReturn("Partner 1")
//    when(amountDeclaredRs.getInt("type_of_business")).thenReturn(1)
//
//    // Group cursor
//    when(groupRs.next()).thenReturn(true, false)
//    when(groupRs.getString("names_of_group_mems")).thenReturn("Group 1")
//
//    // Return periods cursor
//    when(returnPeriodRs.next()).thenReturn(true, false)
//    when(returnPeriodRs.getDate("return_period_end_date"))
//      .thenReturn(Date.valueOf("2023-12-31"))
//
//    val result = repository.getMgdCertificate(regNumber).futureValue
//
//    result.regNumber   shouldBe regNumber
//    result.businessName   shouldBe Some("Test Business")
//    result.groupReg       shouldBe "Y"
//    result.noOfGroupMems  shouldBe Some(2)
//    result.dateCertIssued shouldBe Some(issuedDate)
//
//    result.partMembers.size          shouldBe 1
//    result.groupMembers.size         shouldBe 1
//    result.returnPeriodEndDates.size shouldBe 1
//
//    verify(amountDeclaredRs).close()
//    verify(groupRs).close()
//    verify(returnPeriodRs).close()
//    verify(mockCs).close()
//  }
//
//  it should "return empty lists when cursors are empty" in {
//
//    when(mockCs.getObject(24)).thenReturn(amountDeclaredRs)
//    when(mockCs.getObject(27)).thenReturn(groupRs)
//    when(mockCs.getObject(29)).thenReturn(returnPeriodRs)
//
//    when(amountDeclaredRs.next()).thenReturn(false)
//    when(groupRs.next()).thenReturn(false)
//    when(returnPeriodRs.next()).thenReturn(false)
//
//    val result = repository.getMgdCertificate("XWM00000001770").futureValue
//
//    result.partMembers          shouldBe empty
//    result.groupMembers         shouldBe empty
//    result.returnPeriodEndDates shouldBe empty
//
//    verify(amountDeclaredRs).close()
//    verify(groupRs).close()
//    verify(returnPeriodRs).close()
//    verify(mockCs).close()
//  }
//
//  it should "handle null cursors safely" in {
//
//    when(mockCs.getObject(24)).thenReturn(null)
//    when(mockCs.getObject(27)).thenReturn(null)
//    when(mockCs.getObject(29)).thenReturn(null)
//
//    val result = repository.getMgdCertificate("XWM00000001770").futureValue
//
//    result.partMembers          shouldBe empty
//    result.groupMembers         shouldBe empty
//    result.returnPeriodEndDates shouldBe empty
//
//    verify(mockCs).close()
//  }
//
//  it should "close resources when execute throws exception" in {
//
//    when(mockCs.execute()).thenThrow(new RuntimeException("DB error"))
//
//    val ex = repository.getMgdCertificate("XWM00000001770").failed.futureValue
//    ex.getMessage should include("DB error")
//
//    verify(mockCs).close()
//  }
}
