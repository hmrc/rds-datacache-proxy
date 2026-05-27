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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.services

import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, UnexpectedError}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.utils.GamblingUtils.regNumberPattern

import scala.concurrent.{ExecutionContext, Future}

trait BaseService extends Logging {

  def withValidParams[T](
    regime: String,
    regNumber: String,
    baseText: String
  )(
    ifValid: (Regime, String) => Future[T]
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[StatementError, T]] =
    lazy val reqText = s"regime=$regime regNumber=$regNumber"
    logger.info(s"[$baseText] $reqText")

    Regime.fromString(regime.trim) match {
      case Right(regime) =>
        if (!regNumberPattern.matcher(regNumber).matches())
          logger.warn(s"[$baseText] Invalid pattern for regNumber=$regNumber")
          Future.successful(Left(InvalidRegNumber))
        else
          ifValid(regime, regNumber)
            .map(summary => Right(summary))
            .recover { case ex: Exception =>
              logger.error(s"[$baseText] Unexpected error $reqText", ex)
              Left(UnexpectedError)
            }
      case Left(error) =>
        logger.error(s"[$baseText] Invalid Regime Code $regime")
        Future.successful(Left(error))
    }
}
