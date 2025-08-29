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

import java.time.LocalDateTime
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.rdsdatacacheproxy.models.MonthlyReturn

class MonthlyReturnSpec
  extends AnyWordSpec
    with Matchers:

  val dateTime: LocalDateTime = LocalDateTime.parse("2025-01-01T00:00:00")

  private val lastUpdateJsonString: String = Json.toJson(dateTime).toString

  val mrJsonAsString: String =
    s"""
       |{
       |  "monthlyReturnId": 1,
       |  "taxYear": 2025,
       |  "taxMonth": 7,
       |  "nilReturnIndicator": "N",
       |  "decEmpStatusConsidered": "Y",
       |  "decAllSubsVerified": "Y",
       |  "decInformationCorrect": "Y",
       |  "decNoMoreSubPayments": "N",
       |  "decNilReturnNoPayments": "N",
       |  "status": "SUBMITTED",
       |  "lastUpdate": $lastUpdateJsonString,
       |  "amendment": "N",
       |  "supersededBy": 999
       |}""".stripMargin.filterNot(_.isWhitespace)

  val mrModel: MonthlyReturn = MonthlyReturn(
    monthlyReturnId        = 1L,
    taxYear                = 2025,
    taxMonth               = 7,
    nilReturnIndicator     = Some("N"),
    decEmpStatusConsidered = Some("Y"),
    decAllSubsVerified     = Some("Y"),
    decInformationCorrect  = Some("Y"),
    decNoMoreSubPayments   = Some("N"),
    decNilReturnNoPayments = Some("N"),
    status                 = Some("SUBMITTED"),
    lastUpdate             = Some(dateTime),
    amendment              = Some("N"),
    supersededBy           = Some(999L)
  )

  val mrJson: JsValue = Json.parse(mrJsonAsString)

  "MonthlyReturn" should :
    "read JSON correctly" in :
      Json.fromJson[MonthlyReturn](mrJson).get shouldBe mrModel

    "write JSON correctly" in :
      Json.toJson(mrModel) shouldBe mrJson
