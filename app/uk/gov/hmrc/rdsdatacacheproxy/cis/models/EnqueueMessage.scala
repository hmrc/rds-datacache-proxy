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

package uk.gov.hmrc.rdsdatacacheproxy.cis.models

import play.api.libs.json.*

final case class EnqueueMessage(
  sender: String,
  queueName: String,
  replyQueue: String,
  correlationID: String,
  filter: String,
  payload: Map[String, String]
)

object EnqueueMessage {
  implicit private val reads: Reads[EnqueueMessage] = Json
    .reads[EnqueueMessage]
    .filter(JsonValidationError("payload must not be empty"))(_.payload.nonEmpty)

  implicit private val writes: OWrites[EnqueueMessage] = Json.writes[EnqueueMessage]

  implicit val format: OFormat[EnqueueMessage] = OFormat(reads, writes)
}
