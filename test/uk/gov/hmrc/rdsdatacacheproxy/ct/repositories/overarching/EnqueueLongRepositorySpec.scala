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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.overarching

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.overarching.EnqueueHelper

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class EnqueueLongRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with EnqueueHelper {

  var db: Database = _
  var repo: EnqueueLongRepositoryImpl = _
  var mockConnection: java.sql.Connection = _
  var mockCS: CallableStatement = _
  var rs: ResultSet = _

  before {
    db             = mock(classOf[Database])
    mockConnection = mock(classOf[java.sql.Connection])
    mockCS         = mock(classOf[CallableStatement])
    rs             = mock(classOf[ResultSet])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCS)

    repo = new EnqueueLongRepositoryImpl(db)
  }

  "getEnqueueLong" - {
    "return enqueue long" in {
      when(mockCS.getInt(10)).thenReturn(100)

      val result = repo.getEnqueueLong(12345, "sender1", "queue1", "replyQueue1", "correlationId1", "filter1", "key1", 1, 1010).futureValue
      result shouldBe enqueue

      verify(mockConnection).prepareCall("{call udas_queue.enqueue_long(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setLong(1, 12345)
      verify(mockCS).setString(2, "sender1")
      verify(mockCS).setString(3, "queue1")
      verify(mockCS).setString(4, "replyQueue1")
      verify(mockCS).setString(5, "correlationId1")
      verify(mockCS).setString(6, "filter1")
      verify(mockCS).setString(7, "key1")
      verify(mockCS).setInt(8, 1)
      verify(mockCS).setInt(9, 1010)

      verify(mockCS).registerOutParameter(10, java.sql.Types.NUMERIC)

      verify(mockCS).execute()

      verify(mockCS).close()
    }

    "return an exception and close the connection" in {
      when(mockCS.execute()).thenThrow(new RuntimeException("DB error"))

      val ex = repo.getEnqueueLong(67890, "senderNA", "queueNA", "replyQueueNA", "correlationIdNA", "filterNA", "keyNA", 2, 2020).failed.futureValue
      ex.getMessage should include("DB error")

      verify(mockCS).close()
    }
  }
}
