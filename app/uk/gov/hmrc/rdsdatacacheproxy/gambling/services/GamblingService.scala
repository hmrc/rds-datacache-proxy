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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingError.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GamblingService @Inject() (
  repository: GamblingDataSource
)(implicit ec: ExecutionContext)
    extends Logging {

  private val mgdRegNumberPattern = "^[A-Z]{3}[0-9]{11}$".r.pattern

  def getReturnSummary(rawMgdRegNumber: String)(implicit hc: HeaderCarrier): Future[Either[GamblingError, ReturnSummary]] = {

    val mgdRegNumber = rawMgdRegNumber.trim.toUpperCase

    if (!mgdRegNumberPattern.matcher(mgdRegNumber).matches()) {
      logger.warn(s"[GamblingService][getReturnSummary] Invalid pattern for mgdRegNumber=$mgdRegNumber")
      Future.successful(Left(InvalidMgdRegNumber))
    } else {

      repository
        .getReturnSummary(mgdRegNumber)
        .map(summary => Right(summary))
        .recover { case ex: Exception =>
          logger.error(s"[GamblingService][getReturnSummary] Unexpected error mgdRegNumber=$mgdRegNumber", ex)
          Left(UnexpectedError)
        }
    }
  }

  def getBusinessName(rawMgdRegNumber: String)(implicit hc: HeaderCarrier): Future[Either[GamblingError, BusinessName]] = {

    val mgdRegNumber = rawMgdRegNumber.trim.toUpperCase

    if (!mgdRegNumberPattern.matcher(mgdRegNumber).matches()) {
      logger.warn(s"[GamblingService][getBusinessName] Invalid pattern for mgdRegNumber=$mgdRegNumber")
      Future.successful(Left(InvalidMgdRegNumber))
    } else {

      repository
        .getBusinessName(mgdRegNumber)
        .map(summary => Right(summary))
        .recover { case ex: Exception =>
          logger.error(s"[GamblingService][getBusinessName] Unexpected error mgdRegNumber=$mgdRegNumber", ex)
          Left(UnexpectedError)
        }
    }
  }

  def getBusinessDetails(rawMgdRegNumber: String)(implicit hc: HeaderCarrier): Future[Either[GamblingError, BusinessDetails]] = {

    val mgdRegNumber = rawMgdRegNumber.trim.toUpperCase

    if (!mgdRegNumberPattern.matcher(mgdRegNumber).matches()) {
      logger.warn(s"[GamblingService][getBusinessDetails] Invalid pattern for mgdRegNumber=$mgdRegNumber")
      Future.successful(Left(InvalidMgdRegNumber))
    } else {

      repository
        .getBusinessDetails(mgdRegNumber)
        .map(summary => Right(summary))
        .recover { case ex: Exception =>
          logger.error(s"[GamblingService][getBusinessDetails] Unexpected error mgdRegNumber=$mgdRegNumber", ex)
          Left(UnexpectedError)
        }
    }
  }

  def getMgdCertificate(rawMgdRegNumber: String)(implicit hc: HeaderCarrier): Future[Either[GamblingError, MgdCertificate]] = {

    val mgdRegNumber = rawMgdRegNumber.trim.toUpperCase

    if (!mgdRegNumberPattern.matcher(mgdRegNumber).matches()) {
      logger.warn(s"[GamblingService][getMgdCertificate] Invalid pattern mgdRegNumber=$mgdRegNumber")
      Future.successful(Left(InvalidMgdRegNumber))
    } else {

      repository
        .getMgdCertificate(mgdRegNumber)
        .map { certificate =>
          Right(certificate)
        }
        .recover { case ex: Exception =>
          logger.error(
            s"[GamblingService][getMgdCertificate] Unexpected error mgdRegNumber=$mgdRegNumber",
            ex
          )
          Left(UnexpectedError)
        }
    }
  }

}
