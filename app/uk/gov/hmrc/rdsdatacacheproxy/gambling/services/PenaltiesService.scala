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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Penalties, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.PenaltiesDataSource
import uk.gov.hmrc.rdsdatacacheproxy.gambling.utils.GamblingUtils.regNumberPattern

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesService @Inject() (
                                   repository: PenaltiesDataSource
                                 )(implicit ec: ExecutionContext)
  extends Logging {

  def getPenalties(rawRegime: String, rawRegNumber: String, paginationStart: Int, paginationMaxRows: Int)(implicit
                                                                                                          hc: HeaderCarrier
  ): Future[Either[StatementError, Penalties]] = {

    lazy val reqText = s"regime=$rawRegime regNumber=$rawRegNumber pageNo=$paginationStart pageSize=$paginationMaxRows"
    logger.info(s"[PenaltiesController][getPenalties] $reqText")
    val regNumber = rawRegNumber.trim.toUpperCase
    val regime = Regime.fromString(rawRegime.trim)

    if (regime.isLeft)
      logger.error(s"[PenaltiesService][getPenalties] Invalid Regime Code $reqText")
      Future.successful(Left(InvalidRegimeCode))
    else if (!regNumberPattern.matcher(regNumber).matches())
      logger.warn(s"[PenaltiesService][getPenalties] Invalid pattern for regNumber=$regNumber")
      Future.successful(Left(InvalidRegNumber))
    else
      repository
        .getPenalties(regNumber, paginationStart, paginationMaxRows)
        .map(summary => Right(summary))
        .recover { case ex: Exception =>
          logger.error(s"[PenaltiesService][getPenalties] Unexpected error $reqText", ex)
          Left(UnexpectedError)
        }
  }
}