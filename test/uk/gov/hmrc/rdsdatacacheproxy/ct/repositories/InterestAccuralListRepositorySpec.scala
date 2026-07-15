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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, times, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class InterestAccuralListRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: InterestAccuralListDatacacheRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _
  var rs: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    rs = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new InterestAccuralListDatacacheRepositoryImpl(db)
  }

  "getInterestAccuralList" should "return empty list" in {

    when(mockCallableStatement.getObject(eqTo(3), eqTo(classOf[ResultSet]))).thenReturn(rs)
    when(rs.next()).thenReturn(false) // return empty list

    val taxRef: Long = 17L
    val accPeriod: Long = 2L
    val interestType: String = "IDE"

    val result = repository.getInterestAccuralList(taxRef, accPeriod, interestType).futureValue
    result shouldBe List.empty

    verify(mockConnection).prepareCall("{call CT_DC_PK.getInterestAccrualList(?, ?, ?, ?)}")

    verify(mockCallableStatement).setLong(1, taxRef)
    verify(mockCallableStatement).setLong(2, accPeriod)
    verify(mockCallableStatement).setString(3, interestType)

    verify(mockCallableStatement).registerOutParameter(4, OracleTypes.CURSOR)

    verify(mockCallableStatement).execute()

    verify(rs, times(1)).next()

    verify(mockCallableStatement).close()
  }
}
