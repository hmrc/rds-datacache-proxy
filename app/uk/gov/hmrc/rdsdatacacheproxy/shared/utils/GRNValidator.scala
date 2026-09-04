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

import java.util.regex.Pattern

object GRNValidator extends Logging {
  val regNumberPatternGTR: Pattern = "^X[A-Z]{1}[A-Z]{1}[0-9]{11}$".r.pattern
  private val regNumberPatternMGD: Pattern = "^X[A-Za-z]M[0-9]{11}$".r.pattern
  private val REF_NO_LENGTH = 7

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
    List(WEIGHT_10, WEIGHT_11, WEIGHT_12, WEIGHT_13, WEIGHT_8, WEIGHT_7, WEIGHT_6, WEIGHT_5, WEIGHT_4, WEIGHT_3, WEIGHT_2)
  private val checkChars = "ABCDEFGHXJKLMNYPQRSTZVW"

  def validateRegNoRegime(regime: Regime, regNum: String): Either[StatementError, Unit] = {
    validateRegNum(regime, regNum) match
      case Left(err) => Left(err)
      case Right(()) =>
        validateRegime(regime, regNum) match
          case Left(err) => Left(err)
          case Right(()) => Right(())
  }

  def validateRegNum(regime: Regime, regNumber: String): Either[StatementError, Unit] = {
    val regNum = regNumber.toUpperCase().trim
    if (regNum.length != 14) {
      logger.warn(s"validateRegNum '$regNum' is not 14 chars")
      Left(InvalidRegNumber)
    } else if (regime == Regime.MGD) mgdRegNumValid(regNum)
    else gtrRegNumValid(regNum)
  }

  def validateRegime(regime: Regime, regNumber: String): Either[StatementError, Unit] =
    val regNum = regNumber.toUpperCase().trim
    if (regime == Regime.MGD) Right(())
    else {
      for {
        _                <- ensure(regNumberPatternGTR.matcher(regNum).matches(), s"[validateRegime] RegNum is invalid '$regNum'", InvalidRegNumber)
        calculatedRegime <- Right(Regime.fromRegNum(regNum.takeRight(REF_NO_LENGTH).toLong))
        _ <- ensure(calculatedRegime.contains(regime),
                    s"[validateRegime] Regime does not match RegNum $regime calc=$calculatedRegime $regNum",
                    InvalidRegimeCode
                   )
      } yield ()
    }

  private def mgdRegNumValid(regNum: String): Either[StatementError, Unit] =
    ensure(regNumberPatternMGD.matcher(regNum).matches(), s"validateRegNum MGD '$regNum' does not match regEx", InvalidRegNumber)

  private def gtrRegNumValid(regNum: String): Either[StatementError, Unit] =
    for {
      _         <- ensure(regNumberPatternGTR.matcher(regNum).matches(), s"validateRegNum GTR '$regNum' does not match regEx", InvalidRegNumber)
      checkChar <- Right(expectedCheckChar(regNum))
      _ <- ensure(regNum.charAt(1) == checkChar,
                  s"validateRegNum GTR '$regNum' has invalid check char ${regNum.charAt(1)}, should be=$checkChar",
                  InvalidRegNumber
                 )
    } yield ()

  private def expectedCheckChar(regNum: String): Char = {
    val char3 = (regNum.charAt(2).toInt - 32) * WEIGHT_9
    val sum = List.range(3, 14).map(x => weights(x - 3) * regNum.charAt(x).asDigit).sum + char3
    checkChars.charAt(sum % 23)
  }

  private def ensure(condition: Boolean, warning: String, error: StatementError): Either[StatementError, Unit] =
    if (condition) Right(())
    else {
      logger.warn(warning)
      Left(error)
    }
}
