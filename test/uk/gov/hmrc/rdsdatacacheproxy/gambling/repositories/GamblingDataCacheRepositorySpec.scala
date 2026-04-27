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

import java.sql.{CallableStatement, Connection, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GamblingDataCacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: GamblingDataCacheRepository = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)
    when(mockCallableStatement.getObject(2)).thenReturn(mockResultSet)

    repository = new GamblingDataCacheRepository(db)
  }

  private def verifyDbSetup(mgdRegNumber: String): Unit = {
    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getObject(2)
  }

  private def verifyCleanup(rsOpened: Boolean = true): Unit = {
    verify(mockCallableStatement, times(1)).close()
    if (rsOpened) {
      verify(mockResultSet, times(1)).close()
    }
  }

  "getReturnSummary" should "return ReturnSummary when stored procedure returns data" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockResultSet.next()).thenReturn(true)
    when(mockResultSet.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(mockResultSet.getInt("RETURNS_DUE")).thenReturn(5)
    when(mockResultSet.getInt("RETURNS_OVERDUE")).thenReturn(2)

    val result = repository.getReturnSummary(mgdRegNumber).futureValue

    result shouldBe ReturnSummary(
      mgdRegNumber   = mgdRegNumber,
      returnsDue     = 5,
      returnsOverdue = 2
    )
    verifyDbSetup(mgdRegNumber)

    verify(mockResultSet).next()
    verify(mockResultSet).getString("MGD_REG_NUMBER")
    verify(mockResultSet).getInt("RETURNS_DUE")
    verify(mockResultSet).getInt("RETURNS_OVERDUE")

    verifyCleanup()
  }

  "getBusinessName" should "return Business Name when stored procedure returns data" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockResultSet.next()).thenReturn(true)
    when(mockResultSet.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(mockResultSet.getString("SOLE_PROP_TITLE")).thenReturn("Mr")
    when(mockResultSet.getString("SOLE_PROP_FIRST_NAME")).thenReturn("Foo")
    when(mockResultSet.getString("SOLE_PROP_MIDDLE_NAME")).thenReturn("B")
    when(mockResultSet.getString("SOLE_PROP_LAST_NAME")).thenReturn("Bar")
    when(mockResultSet.getString("BUSINESS_NAME")).thenReturn("Foo Bar Co.")
    when(mockResultSet.getInt("BUSINESS_TYPE")).thenReturn(1)
    when(mockResultSet.getString("TRADING_NAME")).thenReturn("Foobar")
    when(mockResultSet.getDate("SYSTEM_DATE")).thenReturn(java.sql.Date.valueOf("2024-04-21"))

    val result = repository.getBusinessName(mgdRegNumber).futureValue

    result shouldBe BusinessName(
      mgdRegNumber      = mgdRegNumber,
      solePropTitle     = "Mr",
      solePropFirstName = "Foo",
      solePropMidName   = Some("B"),
      solePropLastName  = "Bar",
      businessName      = "Foo Bar Co.",
      businessType      = 1,
      tradingName       = "Foobar",
      systemDate        = Some(LocalDate.of(2024, 4, 21))
    )
    verifyDbSetup(mgdRegNumber)

    verify(mockResultSet).next()
    verify(mockResultSet).getString("MGD_REG_NUMBER")
    verify(mockResultSet).getString("SOLE_PROP_TITLE")
    verify(mockResultSet).getString("SOLE_PROP_FIRST_NAME")
    verify(mockResultSet).getString("SOLE_PROP_MIDDLE_NAME")
    verify(mockResultSet).getString("SOLE_PROP_LAST_NAME")
    verify(mockResultSet).getString("BUSINESS_NAME")
    verify(mockResultSet).getInt("BUSINESS_TYPE")
    verify(mockResultSet).getString("TRADING_NAME")
    verify(mockResultSet).getDate("SYSTEM_DATE")

    verifyCleanup()
  }

  it should "handle null values in result set safely" in {
    when(mockResultSet.next()).thenReturn(true)
    when(mockResultSet.getString("MGD_REG_NUMBER")).thenReturn(null)

    val result = repository.getReturnSummary("XWM12345678901").futureValue

    result.mgdRegNumber shouldBe null // or expected behaviour
  }

  it should "handle zero counts correctly" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockResultSet.next()).thenReturn(true)
    when(mockResultSet.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(mockResultSet.getInt("RETURNS_DUE")).thenReturn(0)
    when(mockResultSet.getInt("RETURNS_OVERDUE")).thenReturn(0)

    val result = repository.getReturnSummary(mgdRegNumber).futureValue

    verifyDbSetup(mgdRegNumber)

    verify(mockResultSet).next()
    verify(mockResultSet).getString("MGD_REG_NUMBER")
    verify(mockResultSet).getInt("RETURNS_DUE")
    verify(mockResultSet).getInt("RETURNS_OVERDUE")

    verifyCleanup()

    result shouldBe ReturnSummary(
      mgdRegNumber   = mgdRegNumber,
      returnsDue     = 0,
      returnsOverdue = 0
    )
  }

  it should "throw exception when result set is empty (returnSummary)" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockResultSet.next()).thenReturn(false)

    val exception = intercept[RuntimeException] {
      repository.getReturnSummary(mgdRegNumber).futureValue
    }

    exception.getMessage should include("Empty result set")

    verifyDbSetup(mgdRegNumber)
    verifyCleanup()
  }

  it should "throw exception when result set is empty (businessName)" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockResultSet.next()).thenReturn(false)

    val exception = intercept[RuntimeException] {
      repository.getBusinessName(mgdRegNumber).futureValue
    }

    exception.getMessage should include("Empty result set")

    verifyDbSetup(mgdRegNumber)
    verifyCleanup()
  }

  it should "fail when cursor is not a ResultSet" in {
    when(mockCallableStatement.getObject(2)).thenReturn("invalid")

    val ex = repository.getReturnSummary("XWM12345678901").failed.futureValue
    ex shouldBe a[ClassCastException]
  }

  it should "close resources even when exception occurs" in {

    val mgdRegNumber = "XWM12345678901"
    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

    val ex = repository.getReturnSummary(mgdRegNumber).failed.futureValue
    ex.getMessage should include("DB error")

    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockResultSet, never()).close()

    verifyCleanup(rsOpened = false)
  }

  it should "close ResultSet when exception occurs after it is opened" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockCallableStatement.getObject(2)).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenThrow(new RuntimeException("RS error"))

    val ex = repository.getReturnSummary(mgdRegNumber).failed.futureValue

    ex          shouldBe a[RuntimeException]
    ex.getMessage should include("RS error")

    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getObject(2)

    // Critical verification
    verify(mockResultSet).close()
    verify(mockCallableStatement).close()
  }

  it should "throw exception when cursor is null (returnSummary)" in {

    val mgdRegNumber = "ABC12345678901"

    when(mockCallableStatement.getObject(2)).thenReturn(null)

    val exception = intercept[RuntimeException] {
      repository.getReturnSummary(mgdRegNumber).futureValue
    }

    exception.getMessage should include("Null cursor")

    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getObject(2)

    verify(mockCallableStatement).close()
  }

  it should "throw exception when cursor is null (businessName)" in {

    val mgdRegNumber = "ABC12345678901"

    when(mockCallableStatement.getObject(2)).thenReturn(null)

    val exception = intercept[RuntimeException] {
      repository.getBusinessName(mgdRegNumber).futureValue
    }

    exception.getMessage should include("Null cursor")

    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getObject(2)

    verify(mockCallableStatement).close()
  }

  "getBusinessDetails" should "return BusinessDetails when stored procedure returns data" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockResultSet.next()).thenReturn(true)
    when(mockResultSet.getString("MGD_REG_NUMBER")).thenReturn(mgdRegNumber)
    when(mockResultSet.getInt("BUSINESS_TYPE")).thenReturn(5)
    when(mockResultSet.getInt("CURRENTLY_REGISTERED")).thenReturn(2)
    when(mockResultSet.getString("GROUP_REG")).thenReturn("foo")
    when(mockResultSet.getDate("DATE_OF_REGISTRATION")).thenReturn(java.sql.Date.valueOf("2024-04-21"))
    when(mockResultSet.getString("BUSINESS_PARTNER_NUMBER")).thenReturn("bar")
    when(mockResultSet.getDate("SYSTEM_DATE")).thenReturn(java.sql.Date.valueOf("2024-04-21"))

    val result = repository.getBusinessDetails(mgdRegNumber).futureValue

    result shouldBe BusinessDetails(
      mgdRegNumber          = mgdRegNumber,
      businessType          = 5,
      currentlyRegistered   = 2,
      groupReg              = "foo",
      dateOfRegistration    = Some(LocalDate.of(2024, 4, 21)),
      businessPartnerNumber = "bar",
      systemDate            = Some(LocalDate.of(2024, 4, 21))
    )
    verifyDbSetup(mgdRegNumber)

    verify(mockResultSet).next()
    verify(mockResultSet).getString("MGD_REG_NUMBER")
    verify(mockResultSet).getInt("BUSINESS_TYPE")
    verify(mockResultSet).getInt("CURRENTLY_REGISTERED")
    verify(mockResultSet).getString("GROUP_REG")
    verify(mockResultSet).getDate("DATE_OF_REGISTRATION")
    verify(mockResultSet).getString("BUSINESS_PARTNER_NUMBER")
    verify(mockResultSet).getDate("SYSTEM_DATE")

    verifyCleanup()
  }

  it should "handle null values in result set safely" in {
    when(mockResultSet.next()).thenReturn(true)
    when(mockResultSet.getString("MGD_REG_NUMBER")).thenReturn(null)

    val result = repository.getBusinessDetails("XWM12345678901").futureValue

    result.mgdRegNumber shouldBe null
  }

  it should "throw exception when result set is empty" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockResultSet.next()).thenReturn(false)

    val exception = intercept[RuntimeException] {
      repository.getBusinessDetails(mgdRegNumber).futureValue
    }

    exception.getMessage should include("Empty result set")

    verifyDbSetup(mgdRegNumber)
    verifyCleanup()
  }

  it should "fail when cursor is not a ResultSet" in {
    when(mockCallableStatement.getObject(2)).thenReturn("invalid")

    val ex = repository.getBusinessDetails("XWM12345678901").failed.futureValue
    ex shouldBe a[ClassCastException]
  }

  it should "close resources even when exception occurs" in {

    val mgdRegNumber = "XWM12345678901"
    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

    val ex = repository.getBusinessDetails(mgdRegNumber).failed.futureValue
    ex.getMessage should include("DB error")

    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockResultSet, never()).close()

    verifyCleanup(rsOpened = false)
  }

  it should "close ResultSet when exception occurs after it is opened" in {

    val mgdRegNumber = "XWM12345678901"

    when(mockCallableStatement.getObject(2)).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenThrow(new RuntimeException("RS error"))

    val ex = repository.getBusinessDetails(mgdRegNumber).failed.futureValue

    ex          shouldBe a[RuntimeException]
    ex.getMessage should include("RS error")

    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getObject(2)

    verify(mockResultSet).close()
    verify(mockCallableStatement).close()
  }

  it should "throw exception when cursor is null" in {

    val mgdRegNumber = "ABC12345678901"

    when(mockCallableStatement.getObject(2)).thenReturn(null)

    val exception = intercept[RuntimeException] {
      repository.getBusinessDetails(mgdRegNumber).futureValue
    }

    exception.getMessage should include("Null cursor")

    verify(mockCallableStatement).setString(1, mgdRegNumber)
    verify(mockCallableStatement).registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getObject(2)

    verify(mockCallableStatement).close()
  }

}
