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
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.{Enqueue, EnqueueMessageHeaderRequest}

import java.sql.Types
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


@ImplementedBy(classOf[EnqueueMessageHeaderRepositoryImpl])
trait EnqueueMessageHeaderRepository {
  def enqueueMessageHeader(request: EnqueueMessageHeaderRequest): Future[Enqueue]
}

class EnqueueMessageHeaderRepositoryImpl @Inject()(
                                                    @NamedDatabase("cis") db: Database
                                                  )(implicit ec: ExecutionContext)
  extends EnqueueMessageHeaderRepository
    with Logging {

  override def enqueueMessageHeader(request: EnqueueMessageHeaderRequest): Future[Enqueue] = {
  Future {
    db.withConnection {
      connect  =>
        val cs = connect .prepareCall("{call udas_queue.enqueue_message_header(?, ?, ?, ?, ?, ?)}")

        try {
          cs.setString(1, request.sender)
          cs.setString(2, request.queue)
          cs.setString(3, request.replyQueue)
          cs.setString(4, request.correlationId)
          cs.setString(5, request.filter)
          cs.registerOutParameter(6, Types.NUMERIC)

          cs.execute()

          Enqueue(
            messageID = cs.getInt(6)
          )
        } finally {
          cs.close()
        }
    }
  }

  }

}
