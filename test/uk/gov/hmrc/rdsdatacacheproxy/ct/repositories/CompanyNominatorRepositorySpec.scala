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

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CompanyNominator

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class CompanyNominatorRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter {

  val companyNominatorTrue: CompanyNominator = CompanyNominator(isParticipator = "Y")
  val companyNominatorFalse: CompanyNominator = CompanyNominator(isParticipator = "N")

  var db: Database = _
  var repo: CompanyNominatorRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    rs                    = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repo = new CompanyNominatorRepositoryImpl(db)
  }

  "getIsCompanyNominatorOfGPA" - {
    "return company nominator when isParticipator is 'Y'" in {
      when(mockCallableStatement.getString(3)).thenReturn("Y")

      val result = repo.getIsCompanyNominatorOfGPA(2L, 3L).futureValue
      result shouldBe companyNominatorTrue

      verify(mockConnection).prepareCall("{call CT_GPA_PK.isCompanyNominatorOfGPA(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 2L)
      verify(mockCallableStatement).setLong(2, 3L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.VARCHAR)
      verify(mockCallableStatement).execute()

      verify(mockCallableStatement).close()
    }

    "return company nominator when isParticipator is 'N'" in {
      when(mockCallableStatement.getString(3)).thenReturn("N")

      val result = repo.getIsCompanyNominatorOfGPA(4L, 2L).futureValue
      result shouldBe companyNominatorFalse

      verify(mockConnection).prepareCall("{call CT_GPA_PK.isCompanyNominatorOfGPA(?, ?, ?)}")

      verify(mockCallableStatement).setLong(1, 4L)
      verify(mockCallableStatement).setLong(2, 2L)

      verify(mockCallableStatement).registerOutParameter(3, OracleTypes.VARCHAR)
      verify(mockCallableStatement).execute()

      verify(mockCallableStatement).close()
    }

    "return an exception and close the connection" in {
      when(mockCallableStatement.execute()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getIsCompanyNominatorOfGPA(1L, 2L).failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCallableStatement).close()
    }
  }
}
