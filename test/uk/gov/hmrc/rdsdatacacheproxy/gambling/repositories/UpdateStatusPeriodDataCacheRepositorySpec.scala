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
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import java.sql.{CallableStatement, Connection}
import scala.concurrent.ExecutionContext.Implicits.global

class UpdateStatusPeriodDataCacheRepositorySpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val mgdDb: MGDDatabase = mock(classOf[Database]).asInstanceOf[MGDDatabase]
  private val gtrDb: GTRDatabase = mock(classOf[Database]).asInstanceOf[GTRDatabase]
  private val mgdMockConnection: Connection = mock(classOf[Connection])
  private val mockCsMgd: CallableStatement = mock(classOf[CallableStatement])
  val repository: UpdateStatusPeriodDataCacheRepository = new UpdateStatusPeriodDataCacheRepository(
    mgdDb = mgdDb,
    gtrDb = gtrDb
  )

  before {
    Mockito.reset(mgdDb, gtrDb, mgdMockConnection, mockCsMgd)

    when(mgdDb.underlying.withConnection(any())).thenAnswer { invocation =>
      val fn = invocation.getArgument(0, classOf[Connection => Any])
      fn(mgdMockConnection)
    }

    when(mgdMockConnection.prepareCall("{ call MGD_DC_RTN_PCK.SET_PERIOD_STATUS(?, ?, ?) }")).thenReturn(mockCsMgd)
  }

  "updateStatusPeriod" should {
    "call the stored procedure with the correct positional bindings and close the statement" in {
      val regNumber = "XWM12345678901"

      repository.updateStatusPeriod(regNumber, 3, 1).futureValue

      verify(mockCsMgd).setString(1, regNumber)
      verify(mockCsMgd).setInt(2, 3)
      verify(mockCsMgd).setInt(3, 1)
      verify(mockCsMgd).execute()
      verify(mockCsMgd).close()
    }

    "close the statement even when execute fails" in {
      val regNumber = "XWM12345678901"
      when(mockCsMgd.execute()).thenThrow(new RuntimeException("DB failure"))

      val exception = repository.updateStatusPeriod(regNumber, 3, 1).failed.futureValue

      exception shouldBe a[RuntimeException]
      verify(mockCsMgd).close()
    }
  }
}
