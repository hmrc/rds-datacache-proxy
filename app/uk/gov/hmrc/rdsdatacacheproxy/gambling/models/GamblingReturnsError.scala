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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models

sealed trait GamblingReturnsError {
  def code: String
  def message: String
}

object GamblingReturnsError {

  case object InvalidRegNumber extends GamblingReturnsError {
    val code = "INVALID_REG_NUMBER"
    val message = "regNumber does not exist"
  }

  case object UnexpectedError extends GamblingReturnsError {
    val code = "UNEXPECTED_ERROR"
    val message = "Unexpected error occurred"
  }

  case object InvalidRegimeCode extends GamblingReturnsError {
    val code = "INVALID_REGIME_CODE"
    val message = "Invalid Regime Code"
  }
}
