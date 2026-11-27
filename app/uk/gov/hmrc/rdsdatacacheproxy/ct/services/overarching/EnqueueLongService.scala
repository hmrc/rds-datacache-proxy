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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.overarching

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.Enqueue
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.overarching.EnqueueLongRepository

import javax.inject.Inject
import scala.concurrent.Future

class EnqueueLongService @Inject() (enqueueLongRepository: EnqueueLongRepository) extends Logging {

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
    logger.info(
      s"Calling repository for messageIdIn: $messageIdIn, sender: $sender, queue: $queue, replyQueue: $replyQueue, correlationId: $correlationId, filter: $filter, key: $key, dataType: $dataType and numberValue: $numberValue"
    )

    enqueueLongRepository.getEnqueueLong(messageIdIn, sender, queue, replyQueue, correlationId, filter, key, dataType, numberValue)
  }
}
