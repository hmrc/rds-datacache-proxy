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
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CompanyDetails, CompanyDetailsResponse}

import java.sql.{CallableStatement, Connection, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class CompanyDetailsRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var repository: CompanyDetailsRepositoryImpl = _
  var db: Database = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new CompanyDetailsRepositoryImpl(db)

  }

  "getCompanyDetails" should "return an empty CompanyDetailsResponse when resultSet is null" in {
    val taxRef = 123456L

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(false)

    val result = repository.getCompanyDetails(taxRef).futureValue

    result shouldBe CompanyDetailsResponse(taxpayerDetails = List.empty)

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).close()

  }
  "getCompanyDetails" should "return CompanyDetailsResponse with multiple CompanyDetails for a given taxPayerReference" in {
    val taxRef = 123456L

    when(mockCallableStatement.getObject(eqTo(2), eqTo(classOf[ResultSet]))).thenReturn(mockResultSet)
    when(mockResultSet.next()).thenReturn(true, true, false)

    when(mockResultSet.getString("ORG_UNIT_ID")).thenReturn("275400", "27600")
    when(mockResultSet.getString("COMPANY_NAME")).thenReturn("M_CT_ORG_221", "M_CT_ORG_223")
    when(mockResultSet.getString("COMPANY_REG_NO")).thenReturn("66000191", "66000192")
    when(mockResultSet.getString("ADDRESS_LINE1")).thenReturn("CT*Company*1", "CT*Company*1")
    when(mockResultSet.getString("ADDRESS_LINE2")).thenReturn("Address3", "CT*Company*1")
    when(mockResultSet.getString("ADDRESS_LINE3")).thenReturn("Address4", "Address4")
    when(mockResultSet.getString("ADDRESS_LINE4")).thenReturn("Address5", "Address6")
    when(mockResultSet.getString("POST_CODE")).thenReturn("TF3 4ER", "TF3 7ZR")

    val companyDetailsResponse = CompanyDetailsResponse(taxpayerDetails =
      List(
        CompanyDetails(
          orgUnitId    = "275400",
          companyName  = "M_CT_ORG_221",
          companyRegNo = Some("66000191"),
          addressLine1 = Some("CT*Company*1"),
          addressLine2 = Some("Address3"),
          addressLine3 = Some("Address4"),
          addressLine4 = Some("Address5"),
          postCode     = Some("TF3 4ER")
        ),
        CompanyDetails(
          orgUnitId    = "27600",
          companyName  = "M_CT_ORG_223",
          companyRegNo = Some("66000192"),
          addressLine1 = Some("CT*Company*1"),
          addressLine2 = Some("CT*Company*1"),
          addressLine3 = Some("Address4"),
          addressLine4 = Some("Address6"),
          postCode     = Some("TF3 7ZR")
        )
      )
    )

    val result = repository.getCompanyDetails(taxRef).futureValue

    result shouldBe companyDetailsResponse

    verify(mockConnection).prepareCall("{call UDAS_CT_ODS_DC.getCTTaxpayer(?,?)}")

    verify(mockCallableStatement).setLong(1, taxRef)

    verify(mockCallableStatement).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCallableStatement).execute()

    verify(mockResultSet, times(3)).next()
    verify(mockCallableStatement).close()
  }

  "getCompanyDetails" should "throw exception and close the connection when exception is returned from DB" in {
    val taxRef = 123456L

    when(mockCallableStatement.execute()).thenThrow(new RuntimeException("Boom"))

    val ex = repository.getCompanyDetails(taxRef).failed.futureValue

    ex.getMessage should include("Boom")

    verify(mockCallableStatement).close()
  }

}
