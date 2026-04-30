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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingReturnsError.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingReturnsDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

private final val Regimes = List("mgd", "gbd", "pbd", "rgd")

class GamblingReturnsService @Inject() (
  repository: GamblingReturnsDataSource
)(implicit ec: ExecutionContext)
    extends Logging {

  private val regNumberPattern = "^[A-Z]{3}[0-9]{11}$".r.pattern

  def getReturnsSubmitted(regime: String, rawRegNumber: String, paginationStart: Int, paginationMaxRows: Int)(implicit
    hc: HeaderCarrier
  ): Future[Either[GamblingReturnsError, ReturnsSubmitted]] = {

    val regNumber = rawRegNumber.trim.toUpperCase

    if (Regimes.contains(regime.trim.toLowerCase())) {
      if (!regNumberPattern.matcher(regNumber).matches()) {
        logger.warn(s"[GamblingReturnsService][getReturnsSubmitted] Invalid pattern for regNumber=$regNumber")
        Future.successful(Left(InvalidRegNumber))
      } else {
        repository
          .getReturnsSubmitted(regNumber, paginationStart, paginationMaxRows)
          .map(summary => Right(summary))
          .recover { case ex: Exception =>
            logger.error(
              s"[GamblingReturnsService][getReturnsSubmitted] Unexpected error regNumber=$regNumber paginationStart=$paginationStart  paginationMaxRows=$paginationMaxRows",
              ex
            )
            Left(UnexpectedError)
          }
      }
    } else {
      logger.error(
        s"[GamblingReturnsService][getReturnsSubmitted] Invalid Regime Code regime=$regime regNumber=$regNumber paginationStart=$paginationStart  paginationMaxRows=$paginationMaxRows"
      )
      Future(Left(InvalidRegimeCode))
    }
  }
}
