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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, concurrent}
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdminRule

import java.math.BigDecimal
import java.sql.{CallableStatement, Date, ResultSet}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class AdministrativeRuleRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: AdministrativeRuleRepositoryImpl = _
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

    repository = new AdministrativeRuleRepositoryImpl(db)
  }

  "getAdminRule" should "return AdminRule ruleNumber = None and ruleDate = None" in {
    val adminRuleKey: String = "START-OF-CTSA"

    when(mockCallableStatement.getBigDecimal(2)).thenReturn(null)
    when(mockCallableStatement.getDate(3)).thenReturn(null)

    val expectedOutput = AdminRule(ruleNumber = None, ruleDate = None)

    val result = repository.getAdminRule(adminRuleKey).futureValue

    result shouldBe expectedOutput

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getAdminRule(?, ?, ?)}")

    verify(mockCallableStatement).setString(1, adminRuleKey)

    verify(mockCallableStatement).registerOutParameter(2, java.sql.Types.NUMERIC)
    verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.DATE)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getAdminRule" should "return AdminRule with both nonempty ruleNumber and ruleDate" in {
    val adminRuleKey: String = "START-OF-CTSA"
    val date: LocalDate = LocalDate.of(2026, 7, 24)
    val ruleNumber: BigDecimal = BigDecimal(124)

    when(mockCallableStatement.getBigDecimal(2)).thenReturn(new BigDecimal(124))
    when(mockCallableStatement.getDate(3)).thenReturn(Date.valueOf(date))

    val expectedOutput = AdminRule(ruleNumber = Some(ruleNumber), ruleDate = Some(date))

    val result = repository.getAdminRule(adminRuleKey).futureValue

    result shouldBe expectedOutput

    result.ruleNumber shouldBe expectedOutput.ruleNumber

    result.ruleDate shouldBe expectedOutput.ruleDate

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getAdminRule(?, ?, ?)}")

    verify(mockCallableStatement).setString(1, adminRuleKey)

    verify(mockCallableStatement).registerOutParameter(2, java.sql.Types.NUMERIC)
    verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.DATE)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getAdminRule" should "return AdminRule with ruleNumber = None and  nonEmpty ruleDate" in {

    val adminRuleKey: String = "START-OF-CTSA"
    val date: LocalDate = LocalDate.of(2026, 7, 24)

    when(mockCallableStatement.getBigDecimal(2)).thenReturn(null)
    when(mockCallableStatement.getDate(3)).thenReturn(Date.valueOf(date))

    val expectedOutput = AdminRule(ruleNumber = None, ruleDate = Some(date))

    val result = repository.getAdminRule(adminRuleKey).futureValue

    result shouldBe expectedOutput

    result.ruleNumber shouldBe expectedOutput.ruleNumber

    result.ruleDate shouldBe expectedOutput.ruleDate

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getAdminRule(?, ?, ?)}")

    verify(mockCallableStatement).setString(1, adminRuleKey)

    verify(mockCallableStatement).registerOutParameter(2, java.sql.Types.NUMERIC)
    verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.DATE)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()

  }

  "getAdminRule" should "return AdminRule with ruleDate = None and nonEmpty ruleNumber" in {
    val adminRuleKey: String = "START-OF-CTSA"
    val ruleNumber: BigDecimal = BigDecimal(124)

    when(mockCallableStatement.getBigDecimal(2)).thenReturn(new BigDecimal(124))
    when(mockCallableStatement.getDate(3)).thenReturn(null)

    val expectedOutput = AdminRule(ruleNumber = Some(ruleNumber), ruleDate = None)

    val result = repository.getAdminRule(adminRuleKey).futureValue

    result shouldBe expectedOutput

    result.ruleNumber shouldBe expectedOutput.ruleNumber

    result.ruleDate shouldBe expectedOutput.ruleDate

    verify(mockConnection).prepareCall("{call CT_LNP_PK.getAdminRule(?, ?, ?)}")

    verify(mockCallableStatement).setString(1, adminRuleKey)

    verify(mockCallableStatement).registerOutParameter(2, java.sql.Types.NUMERIC)
    verify(mockCallableStatement).registerOutParameter(3, java.sql.Types.DATE)

    verify(mockCallableStatement).execute()

    verify(mockCallableStatement).close()
  }

  "getAdminRule" should "close resources when execution throws exception" in {

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Error in DB"))
    val adminRuleKey: String = "START-OF-CTSA"

    val ex = repository.getAdminRule(adminRuleKey).failed.futureValue

    ex.getMessage should include("Error in DB")

    verify(mockCallableStatement).close()
  }

}
