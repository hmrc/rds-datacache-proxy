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

  def withValidParams[T](
    regime: String,
    regNumber: String,
    paginationStart: Int,
    paginationMaxRows: Int,
    baseText: String
  )(
    ifValid: (Regime, String, Int, Int) => Future[T]
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[StatementError, T]] =
    lazy val reqText = s"regime=$regime regNumber=$regNumber pageNo=$paginationStart pageSize=$paginationMaxRows"
    logger.info(s"[$baseText] $reqText")

    Regime.fromString(regime.trim) match {
      case Right(regime) =>
        if (!regNumberPattern.matcher(regNumber).matches())
          logger.warn(s"[$baseText] Invalid pattern for regNumber=$regNumber")
          Future.successful(Left(InvalidRegNumber))
        else
          ifValid(regime, regNumber, paginationStart, paginationMaxRows)
            .map(summary => Right(summary))
            .recover { case ex: Exception =>
              logger.error(s"[$baseText] Unexpected error $reqText", ex)
              Left(UnexpectedError)
            }
      case Left(error) =>
        logger.error(s"[$baseText] Invalid Regime Code $regime")
        Future.successful(Left(error))
    }

  def withValidParams[T](
    regime: String,
    regNumber: String,
    interestId: String,
    paginationStart: Int,
    paginationMaxRows: Int,
    baseText: String
  )(
    ifValid: (Regime, String, String, Int, Int) => Future[T]
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[StatementError, T]] =
    lazy val reqText = s"regime=$regime regNumber=$regNumber interestId=$interestId pageNo=$paginationStart pageSize=$paginationMaxRows"
    logger.info(s"[$baseText] $reqText")

    Regime.fromString(regime.trim) match {
      case Right(regime) =>
        if (!regNumberPattern.matcher(regNumber).matches())
          logger.warn(s"[$baseText] Invalid pattern for regNumber=$regNumber")
          Future.successful(Left(InvalidRegNumber))
        else
          ifValid(regime, regNumber, interestId, paginationStart, paginationMaxRows)
            .map(summary => Right(summary))
            .recover { case ex: Exception =>
              logger.error(s"[$baseText] Unexpected error $reqText", ex)
              Left(UnexpectedError)
            }
      case Left(error) =>
        logger.error(s"[$baseText] Invalid Regime Code $regime")
        Future.successful(Left(error))
    }

  def withValidParams[T](
    regNumber: String,
    sortBy: Option[Int],
    orderBy: Option[String],
    baseText: String
  )(
    ifValid: (String, Int, String) => Future[T]
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[StatementError, T]] =
    lazy val reqText = s"regNumber=$regNumber sortBy=$sortBy orderBy=$orderBy"
    logger.info(s"[$baseText] $reqText")

    if (!regNumberPattern.matcher(regNumber).matches())
      logger.warn(s"[$baseText] Invalid pattern for regNumber=$regNumber")
      Future.successful(Left(InvalidRegNumber))
    else {
      val sort = sortBy match { // 1=PERIOD_START_DATE , 2=SUBMITTED_DATE , else PERIOD_END_DATE
        case s @ (Some(1) | Some(2)) => s.get
        case _                       => 3
      }

      val order = orderBy.map(_.trim.toUpperCase()) match {
        case Some("DESC") => "DESC"
        case _            => "ASC"
      }

      logger.info(s"[$baseText] $reqText sort=$sort order=$order")
      ifValid(regNumber, sort, order)
        .map(summary => Right(summary))
        .recover { case ex: Exception =>
          logger.error(s"[$baseText] Unexpected error $reqText", ex)
          Left(UnexpectedError)
        }
    }

  def withValidParams[T](
    regime: String,
    regNumber: String,
    sortBy: Option[Int],
    orderBy: Option[String],
    baseText: String
  )(
    ifValid: (Regime, String, Int, String) => Future[T]
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[StatementError, T]] =
    lazy val reqText = s"regime=$regime regNumber=$regNumber sortBy=$sortBy orderBy=$orderBy"
    logger.info(s"[$baseText] $reqText")

    Regime.fromString(regime.trim) match {
      case Right(regime) =>
        if (!regNumberPattern.matcher(regNumber).matches())
          logger.warn(s"[$baseText] Invalid pattern for regNumber=$regNumber")
          Future.successful(Left(InvalidRegNumber))
        else
          // 1=period, 2=due date, else status
          val sort = sortBy.filter(s => s == 1 || s == 2).getOrElse(3)
          val order = orderBy.map(_.trim.toUpperCase()).filter(_ == "DESC").getOrElse("ASC")

          logger.info(s"[$baseText] $reqText sort=$sort order=$order")
          ifValid(regime, regNumber, sort, order)
            .map(summary => Right(summary))
            .recover { case ex: Exception =>
              logger.error(s"[$baseText] Unexpected error $reqText", ex)
              Left(UnexpectedError)
            }
      case Left(error) =>
        logger.error(s"[$baseText] Invalid Regime Code $regime")
        Future.successful(Left(error))
    }

  def withValidParams[T](
    regNumber: String,
    consecNo: Int,
    baseText: String
  )(
    ifValid: (String, Int) => Future[T]
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Either[StatementError, T]] =
    lazy val reqText = s"regNumber=$regNumber consecNo=$consecNo"
    logger.info(s"[$baseText] $reqText")

    if (!regNumberPattern.matcher(regNumber).matches())
      logger.warn(s"[$baseText] Invalid pattern for regNumber=$regNumber")
      Future.successful(Left(InvalidRegNumber))
    else
      ifValid(regNumber, consecNo)
        .map(single => Right(single))
        .recover { case ex: Exception =>
          logger.error(s"[$baseText] Unexpected error $reqText", ex)
          Left(UnexpectedError)
        }

}
