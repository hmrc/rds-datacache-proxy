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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.shared.utils.GamblingTestUtil.validResponseSubmittedReturnsSmall

import java.sql.{CallableStatement, Connection, Date, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class SubmittedReturnsDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private var mgdDb: MGDDatabase = _
  private var gtrDb: GTRDatabase = _
  var repository: SubmittedReturnsDataCacheRepository = _
  private var mgdMockConnection: Connection = _
  private var gtrMockConnection: Connection = _
  private var mockCsMgd: CallableStatement = _
  private var mockCsGtr: CallableStatement = _
  var submittedReturnsRs: ResultSet = _

  before {
    gtrDb              = mock(classOf[Database]).asInstanceOf[GTRDatabase]
    mgdDb              = mock(classOf[Database]).asInstanceOf[MGDDatabase]
    mgdMockConnection  = mock(classOf[Connection])
    gtrMockConnection  = mock(classOf[Connection])
    mockCsMgd          = mock(classOf[CallableStatement])
    mockCsGtr          = mock(classOf[CallableStatement])
    submittedReturnsRs = mock(classOf[ResultSet])

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_DC_RTN_PCK.GET_SUBMITTED_RETURNS(?, ?, ?, ?) }")).thenReturn(mockCsMgd)

    repository = new SubmittedReturnsDataCacheRepository(
      mgdDb = mgdDb,
      gtrDb = gtrDb
    )
  }

  "getSubmittedReturns" should {
    "return SubmittedReturns when stored procedure returns data" in {

      val regNumber = "XWM12345678901"

      when(mockCsMgd.getObject(4)).thenReturn(submittedReturnsRs)

      when(submittedReturnsRs.next()).thenReturn(true, false)

      when(submittedReturnsRs.getInt("consec_no")).thenReturn(12345)
      when(submittedReturnsRs.getString("mgd_period")).thenReturn("01/01/2025 - 30/03/2025")
      when(submittedReturnsRs.getDate("submitted_date")).thenReturn(Date.valueOf("2025-04-01"))
      when(submittedReturnsRs.getString("ack_ref")).thenReturn("123456789012345")

      val result = repository.getSubmittedReturns(regNumber, 3, "ASC").futureValue

      result            shouldBe validResponseSubmittedReturnsSmall
      result.items.size shouldBe 1

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, "ASC")
      verify(mockCsMgd).setInt(3, 3)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(4)

      verify(submittedReturnsRs, times(2)).next()
      verify(submittedReturnsRs).getInt("consec_no")
      verify(submittedReturnsRs).getString("mgd_period")
      verify(submittedReturnsRs).getDate("submitted_date")
      verify(submittedReturnsRs).getString("ack_ref")

      verify(submittedReturnsRs).close()
      verify(mockCsMgd).close()
    }

    "return empty SubmittedReturns when regNumber is null" in {
      val regNumber: Null = null
      when(mockCsMgd.getObject(4)).thenReturn(null)
      val result = repository.getSubmittedReturns(regNumber, 3, "ASC").futureValue

      result       shouldBe SubmittedReturns(List())
      result.items shouldBe empty

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, "ASC")
      verify(mockCsMgd).setInt(3, 3)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(4)
      verify(submittedReturnsRs, times(0)).next()
      verify(submittedReturnsRs, times(0)).getDate("submitted_date")
      verify(submittedReturnsRs, times(0)).close()
      verify(mockCsMgd).close()
    }

    "return Empty List when SubmittedReturns result set is empty" in {
      val regNumber = "XWM00000001770"
      when(submittedReturnsRs.next()).thenReturn(false)

      val result = repository.getSubmittedReturns(regNumber, 3, "ASC").futureValue

      result shouldBe SubmittedReturns(List())

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setString(2, "ASC")
      verify(mockCsMgd).setInt(3, 3)
      verify(mockCsMgd).registerOutParameter(4, oracle.jdbc.OracleTypes.CURSOR)
      verify(mockCsMgd).execute()

      verify(mockCsMgd).getObject(4)
      verify(submittedReturnsRs, times(0)).next()
      verify(submittedReturnsRs, times(0)).getDate("submitted_date")
      verify(submittedReturnsRs, times(0)).close()
      verify(mockCsMgd).close()
    }
  }
}
