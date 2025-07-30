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

package uk.gov.hmrc.rdsdatacacheproxy.models


import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.rdsdatacacheproxy.models.DirectDebit.*

import java.time.LocalDateTime

class DirectDebitSpec
  extends AnyWordSpec
    with Matchers:

  val dateTime: LocalDateTime = LocalDateTime.now()

  val jsonAsString: String =
    s"""
      |{
      |  "ddiRefNumber": "refnum",
      |  "submissionDateTime": "$dateTime",
      |  "bankSortCode": "00-00-00",
      |  "bankAccountNumber": "00000000",
      |  "bankAccountName": "BankLtd",
      |  "auDdisFlag": false,
      |  "numberOfPayPlans": 1
      |}""".stripMargin.filterNot(_.isWhitespace)
  val model: DirectDebit = DirectDebit(
      ddiRefNumber = "refnum",
      submissionDateTime = dateTime,
      bankSortCode = "00-00-00",
      bankAccountNumber = "00000000",
      bankAccountName = "BankLtd",
      auDdisFlag = false,
      numberOfPayPlans = 1
    )
  val json: JsValue = Json.parse(jsonAsString)

  "DirectDebit" should :
    "read JSON correctly" in :
      Json.fromJson(json).get shouldBe model

    "write JSON correctly" in :
      Json.toJson(model).toString shouldBe jsonAsString

