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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa;

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.GroupTaxChargesStubData

import java.sql.{CallableStatement, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class GroupTaxChargesRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter with GroupTaxChargesStubData {

  var db: Database = _
  var repository: GroupTaxChargesRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    mockResultSet = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new GroupTaxChargesRepositoryImpl(db)
  }

  "getGPAGroupTaxCharges" should "return GpaGroupTaxCharges containing empty ParticipatorDetails from DB when stored procedure is invoked" in {
    val pGpaUtr = 19L
    val pGppContractVersion = 12L
    val pStartIndex = 21L
    val pCount = 33L

    when(mockCallableStatement.getDate(5)).thenReturn(Date.valueOf("2023-04-05"))
    when(mockCallableStatement.getBigDecimal(6)).thenReturn(BigDecimal(0).bigDecimal)
    when(mockCallableStatement.getBigDecimal(7)).thenReturn(BigDecimal(0).bigDecimal)
    when(mockCallableStatement.getString(8)).thenReturn("PENDING")
    when(mockCallableStatement.getDate(9)).thenReturn(Date.valueOf("2024-03-19"))
    when(mockCallableStatement.getString(10)).thenReturn("NOT-EQUAL")
    when(mockCallableStatement.getLong(11)).thenReturn(1000L)
    when(mockCallableStatement.getInt(12)).thenReturn(0)
    when(mockCallableStatement.getInt(13)).thenReturn(0)

    when(mockCallableStatement.getObject(eqTo(14), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(false)

    val result = repository.getGPAGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount).futureValue

    result shouldBe gpaWithEmptyParticipator

    verify(mockConnection).prepareCall("call CT_GPA_PK.getGPAGroupTaxCharges(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")

    verify(mockCallableStatement).setLong(1, pGpaUtr)
    verify(mockCallableStatement).setLong(2, pGppContractVersion)
    verify(mockCallableStatement).setLong(3, pStartIndex)
    verify(mockCallableStatement).setLong(4, pCount)

    verify(mockCallableStatement).registerOutParameter(14, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(1)).next()

    verify(mockCallableStatement).close()

  }

  "getGPAGroupTaxCharges" should "return GpaGroupTaxCharges containing multiple ParticipatorDetails from DB when stored procedure is invoked" in {
    val pGpaUtr = 19L
    val pGppContractVersion = 12L
    val pStartIndex = 21L
    val pCount = 33L

    when(mockCallableStatement.getDate(5)).thenReturn(Date.valueOf("2023-04-05"))
    when(mockCallableStatement.getBigDecimal(6)).thenReturn(BigDecimal(15000.50).bigDecimal)
    when(mockCallableStatement.getBigDecimal(7)).thenReturn(BigDecimal(3200.75).bigDecimal)
    when(mockCallableStatement.getString(8)).thenReturn("SUBMITTED")
    when(mockCallableStatement.getDate(9)).thenReturn(Date.valueOf("2023-03-01"))
    when(mockCallableStatement.getString(10)).thenReturn("EQUAL")
    when(mockCallableStatement.getLong(11)).thenReturn(200L)
    when(mockCallableStatement.getInt(12)).thenReturn(3)
    when(mockCallableStatement.getInt(13)).thenReturn(3)

    when(mockCallableStatement.getObject(eqTo(14), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, true, false)

    when(mockResultSet.getString("PARTICIPATOR_NAME")).thenReturn("Company A Ltd", "Company B Ltd")
    when(mockResultSet.getLong("PARTICIPATOR_REFERENCE")).thenReturn(1234567890L, 2345678901L)
    when(mockResultSet.getDate("PARTICIPATOR_AP_END_DATE")).thenReturn(Date.valueOf("2023-03-31"), Date.valueOf("2023-03-31"))
    when(mockResultSet.getBigDecimal("PARTICIPATOR_TAX_CHARGE")).thenReturn(BigDecimal(1066.92).bigDecimal, BigDecimal(1280.11).bigDecimal)
    when(mockResultSet.getString("PARTICIPATOR_TAX_CHARGE_PRSNT")).thenReturn("Y", "Y")
    when(mockResultSet.getLong("PARTICIPATOR_ACCOUNTING_PERIOD")).thenReturn(1L, 1L)
    when(mockResultSet.getLong("CONTRACT_VERSION")).thenReturn(1L, 1L)
    when(mockResultSet.getBigDecimal("ALLOCATED_PAYMENT")).thenReturn(BigDecimal(5000.00).bigDecimal, BigDecimal(6000.50).bigDecimal)
    when(mockResultSet.getInt("ALLOCATED_PAYMENT_RECORD_COUNT")).thenReturn(1, 1)

    val result = repository.getGPAGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount).futureValue

    result shouldBe gpaWithNonEmptyParticipator

    verify(mockConnection).prepareCall("call CT_GPA_PK.getGPAGroupTaxCharges(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")

    verify(mockCallableStatement).setLong(1, pGpaUtr)
    verify(mockCallableStatement).setLong(2, pGppContractVersion)
    verify(mockCallableStatement).setLong(3, pStartIndex)
    verify(mockCallableStatement).setLong(4, pCount)

    verify(mockCallableStatement).registerOutParameter(14, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(3)).next()

    verify(mockCallableStatement).close()
  }

  "getGPAGroupTaxCharges" should "return an exception and close the connection when an exception occurs in Downstream services" in {
    val pGpaUtr = 123456789L
    val pGppContractVersion = 12L
    val pStartIndex = 21L
    val pCount = 33L

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

    val ex = repository.getGPAGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount).failed.futureValue
    ex.getMessage should include("DB error")

    verify(mockCallableStatement).close()
  }

}
