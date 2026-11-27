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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.EnqueueMessageHeaderRequest

import java.sql.{CallableStatement, ResultSet}
import scala.concurrent.ExecutionContext.Implicits.global

class EnqueueMessageHeaderRepositorySpec extends AnyFreeSpec with Matchers with BeforeAndAfter with EnqueueHelper {

  var db: Database = _
  var repo: EnqueueMessageHeaderRepositoryImpl = _
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

    repo = new EnqueueMessageHeaderRepositoryImpl(db)
  }

  val validRequest = EnqueueMessageHeaderRequest(
    sender = "Portal",
    queue = "AGTAUTH",
    replyQueue = "",
    correlationId = "",
    filter = "RemoveClient"
  )

  "enqueueMessageHeader" - {
    "return enqueue result" in {
      when(mockCS.getInt(6)).thenReturn(100)

      val result = repo.enqueueMessageHeader(validRequest).futureValue
      result shouldBe enqueue

      verify(mockConnection).prepareCall("{call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?)}")

      verify(mockCS).setString(1, "Portal")
      verify(mockCS).setString(2, "AGTAUTH")
      verify(mockCS).setString(3, "")
      verify(mockCS).setString(4, "")
      verify(mockCS).setString(5, "RemoveClient")

      verify(mockCS).registerOutParameter(6, java.sql.Types.NUMERIC)

      verify(mockCS).execute()

      verify(mockCS).close()
    }

    "return an exception and close the connection" in {
      when(mockCS.execute()).thenThrow(new RuntimeException("DB error"))

      val result = repo.enqueueMessageHeader(validRequest).failed.futureValue
      result.getMessage should include("DB error")

      verify(mockCS).close()
    }
  }
}