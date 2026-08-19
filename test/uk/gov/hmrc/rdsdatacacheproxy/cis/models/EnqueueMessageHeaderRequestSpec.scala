/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class EnqueueMessageHeaderRequestSpec extends AnyWordSpec with Matchers {

  "EnqueueMessageHeaderRequest (JSON)" should {

    "read and write with mandatory fields" in {
      val json = Json.parse("""
          |{
          |  "sender": "Portal",
          |  "queueName": "AGTAUTH",
          |  "replyQueue": "",
          |  "correlationId": "",
          |  "filter": "RemoveClient"
          |}
         """.stripMargin)

      val model = json.as[EnqueueMessageHeaderRequest]
      model.sender mustBe "Portal"
      model.queueName mustBe "AGTAUTH"
      model.replyQueue mustBe ""
      model.correlationId mustBe ""
      model.filter mustBe "RemoveClient"
      Json.toJson(model) mustBe json
    }

    "fail to read missing sender" in {
      val json = Json.parse("""
          |{
          |  "queueName": "AGTAUTH",
          |  "replyQueue": "",
          |  "correlationId": "",
          |  "filter": "RemoveClient"
          |}
               """.stripMargin)

      val result = json.validate[EnqueueMessageHeaderRequest]
      result.isError mustBe true
    }

    "fail to read missing queueName" in {
      val json = Json.parse("""
          |{
          |  "sender": "Portal",
          |  "replyQueue": "",
          |  "correlationId": "",
          |  "filter": "RemoveClient"
          |}
               """.stripMargin)

      val result = json.validate[EnqueueMessageHeaderRequest]
      result.isError mustBe true
    }

    "fail to read missing replyQueue" in {
      val json = Json.parse("""
          |{
          |  "sender": "Portal",
          |  "queueName": "AGTAUTH",
          |  "correlationId": "",
          |  "filter": "RemoveClient"
          |}
               """.stripMargin)

      val result = json.validate[EnqueueMessageHeaderRequest]
      result.isError mustBe true
    }

    "fail to read missing correlationId" in {
      val json = Json.parse("""
          |{
          |  "sender": "Portal",
          |  "queueName": "AGTAUTH",
          |  "replyQueue": "",
          |  "filter": "RemoveClient"
          |}
               """.stripMargin)

      val result = json.validate[EnqueueMessageHeaderRequest]
      result.isError mustBe true
    }

    "fail to read missing filter" in {
      val json = Json.parse("""
          |{
          |  "sender": "Portal",
          |  "queueName": "AGTAUTH",
          |  "replyQueue": "",
          |  "correlationId": ""
          |}
                   """.stripMargin)

      val result = json.validate[EnqueueMessageHeaderRequest]
      result.isError mustBe true
    }
  }
}
