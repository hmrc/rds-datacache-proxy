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

sealed trait StatementError {
  def code: String
  def message: String
}

object StatementError {

  case object InvalidRegNumber extends StatementError {
    val code = "INVALID_REG_NUMBER"
    val message = "regNumber has invalid format"
  }

  case object UnexpectedError extends StatementError {
    val code = "UNEXPECTED_ERROR"
    val message = "Unexpected error occurred"
  }

  case object InvalidRegimeCode extends StatementError {
    val code = "INVALID_REGIME_CODE"
    val message = "Invalid Regime Code"
  }

  case object RecordNotFound extends StatementError {
    val code = "NOT_FOUND"
    val message = "No record found for the given registration number"
  }

  case object BadData extends StatementError {
    val code = "BAD_DATA"
    val message = "Bad data record for the given registration number"
  }
}
