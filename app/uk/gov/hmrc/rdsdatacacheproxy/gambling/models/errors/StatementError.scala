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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors

import play.api.libs.json.{Json, OWrites}

enum StatementError(val code: String, val message: String) {
  case InvalidRegNumber  extends StatementError("INVALID_REG_NUMBER", "regNumber has invalid format")
  case UnexpectedError   extends StatementError("UNEXPECTED_ERROR", "Unexpected error occurred")
  case InvalidRegimeCode extends StatementError("INVALID_REGIME_CODE", "Invalid Regime Code")
  case RecordNotFound
      extends StatementError(
        "NOT_FOUND",
        "No record found for the given registration number"
      )
  case BadData
      extends StatementError(
        "BAD_DATA",
        "Bad data record for the given registration number"
      )
}

object StatementError {
  given OWrites[StatementError] = error => Json.obj("code" -> error.code, "message" -> error.message)
}
