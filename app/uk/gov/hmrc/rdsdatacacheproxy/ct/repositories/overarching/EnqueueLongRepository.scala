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

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.Enqueue

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[EnqueueLongRepositoryImpl])
trait EnqueueLongRepository {
  def getEnqueueLong(messageIdIn: Long,
                     sender: String,
                     queue: String,
                     replyQueue: String,
                     correlationId: String,
                     filter: String,
                     key: String,
                     dataType: Int,
                     numberValue: Int
                    ): Future[Enqueue]
}

class EnqueueLongRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends EnqueueLongRepository
    with Logging {

  def getEnqueueLong(messageIdIn: Long,
                     sender: String,
                     queue: String,
                     replyQueue: String,
                     correlationId: String,
                     filter: String,
                     key: String,
                     dataType: Int,
                     numberValue: Int
                    ): Future[Enqueue] = {
    Future {
      db.withConnection { connect =>
        val sp = connect.prepareCall("{call udas_queue.enqueue_long(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")

        try {
          sp.setLong(1, messageIdIn)
          sp.setString(2, sender)
          sp.setString(3, queue)
          sp.setString(4, replyQueue)
          sp.setString(5, correlationId)
          sp.setString(6, filter)
          sp.setString(7, key)
          sp.setInt(8, dataType)
          sp.setInt(9, numberValue)

          sp.registerOutParameter(10, OracleTypes.NUMERIC)

          sp.execute()

          Enqueue(
            messageID = sp.getInt(10)
          )

        } finally {
          sp.close()
        }
      }
    }
  }
}
