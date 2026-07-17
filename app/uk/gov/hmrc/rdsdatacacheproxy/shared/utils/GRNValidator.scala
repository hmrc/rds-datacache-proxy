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

package uk.gov.hmrc.rdsdatacacheproxy.shared.utils

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.utils.GamblingUtils.regNumberPattern

object GRNValidator extends Logging {
  private val REF_NO_LENGTH = 7
//  private val regEx = "X[A-Z]{1}[A-Z]{1}[0-9]{11}"

  private val WEIGHT_9 = 9
  private val WEIGHT_10 = 10
  private val WEIGHT_11 = 11
  private val WEIGHT_12 = 12
  private val WEIGHT_13 = 13
  private val WEIGHT_8 = 8
  private val WEIGHT_7 = 7
  private val WEIGHT_6 = 6
  private val WEIGHT_5 = 5
  private val WEIGHT_4 = 4
  private val WEIGHT_3 = 3
  private val WEIGHT_2 = 2

  private val weights =
    List(WEIGHT_9, WEIGHT_10, WEIGHT_11, WEIGHT_12, WEIGHT_13, WEIGHT_8, WEIGHT_7, WEIGHT_6, WEIGHT_5, WEIGHT_4, WEIGHT_3, WEIGHT_2)
  private val checkChars = List("A", "B", "C", "D", "E", "F", "G", "H", "X", "J", "K", "L", "M", "N", "Y", "P", "Q", "R", "S", "T", "Z", "V", "W")

  def validateRegNoRegime(regime: Regime, regNum: String, baseText: String): Either[StatementError, Unit] = {
    validateRegNum(regNum, baseText) match
      case Left(err) => Left(err)
      case Right(()) =>
        validateRegime(regime, regNum, baseText) match
          case Left(err) => Left(err)
          case Right(()) => Right(())
  }

  def validateRegNum(regNumber: String, baseText: String): Either[StatementError, Unit] = {
    val regNum = regNumber.toUpperCase().trim
    if (regNum.length == 14) {
      if (regNumberPattern.matcher(regNum).matches()) {
        val char3 = (regNum.substring(2, 3).toCharArray.head.toInt - 32) * WEIGHT_9
        val sum = List.range(1, 11).map(x => weights(x) * regNum.substring(x + 2, x + 3).toInt).sum + char3
        val checkChar = checkChars(sum % 23)
        if (regNum.substring(1, 2).equals(checkChar)) {
          Right(())
        } else {
          logger.warn(s"[$baseText] validateRegNum '$regNum' has invalid check char ${regNum.substring(1, 2)}, should be=$checkChar")
          Left(InvalidRegNumber)
        }
      } else {
        logger.warn(s"[$baseText] validateRegNum '$regNum' does not match regEx")
        Left(InvalidRegNumber)
      }
    } else {
      logger.warn(s"[$baseText] validateRegNum '$regNum' is not 14 chars")
      Left(InvalidRegNumber)
    }
  }

  def validateRegime(regime: Regime, regNumber: String, baseText: String): Either[StatementError, Unit] =
    val regNum = regNumber.toUpperCase().trim
    if (!regime.equals(Regime.MGD)) {
      if (regNumberPattern.matcher(regNum).matches()) {
        val calculatedRegime = regimeFromRegNo(regNum.takeRight(REF_NO_LENGTH).toLong)
        if (!calculatedRegime.equals(regime.toString)) {
          logger.warn(s"[$baseText] validateRegime Regime does not match RegNum $regime calc=$calculatedRegime $regNum")
          Left(InvalidRegimeCode)
        } else {
          logger.warn(s"[$baseText] validateRegime Regime matches RegNum '$regime':'$calculatedRegime' '$regNum'")
          Right(())
        }
      } else {
        logger.warn(s"[$baseText] validateRegime RegNum is invalid '$regNum'")
        Left(InvalidRegNumber)
      }
    } else {
      Right(())
    }

  private def regimeFromRegNo(ref: Long) = {
    if (ref >= 3000000 && ref <= 3199999) {
      "GBD"
    } else if (ref >= 3200000 && ref <= 3399999) {
      "PBD"
    } else if (ref >= 3400000 && ref <= 3599999) {
      "RGD"
    } else {
      ""
    }
  }
}
