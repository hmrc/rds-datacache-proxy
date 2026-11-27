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

package uk.gov.hmrc.rdsdatacacheproxy.ct.stub.overarching

import play.api.libs.json.Json
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.{Enqueue, EnqueueMessageHeaderRequest}


object EnqueueMessageHeaderStubData {

  val validJson = Json.obj(
    "sender" -> "Portal",
    "queue" -> "AGTAUTH",
    "replyQueue" -> "",
    "correlationId" -> "",
    "filter" -> "RemoveClient"
  )


  val enqueue: Enqueue = Enqueue(
    messageID = 100
  )

  def getEnqueue(request: EnqueueMessageHeaderRequest): Enqueue = {
    request match {
      case validJson  => enqueue
      case _ => throw new RuntimeException("Downstream error")
    }
  }
}