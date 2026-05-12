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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Assessments
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.QueryParameterError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.QueryParameterError.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.AssessmentsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.gambling.utils.GamblingUtils.regNumberPattern

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

private final val ValidRegimes = List("mgd", "gbd", "pbd", "rgd")

class AssessmentsService @Inject() (
  repository: AssessmentsDataSource
)(implicit ec: ExecutionContext)
    extends Logging {

  def getOtherAssessments(regime: String, rawRegNumber: String, paginationStart: Int, paginationMaxRows: Int)(implicit
    hc: HeaderCarrier
  ): Future[Either[QueryParameterError, Assessments]] = {

    lazy val reqText = s"regime=$regime regNumber=$rawRegNumber pageNo=$paginationStart pageSize=$paginationMaxRows"
    logger.info(s"[AssessmentsService][getOtherAssessments] $reqText")
    val regNumber = rawRegNumber.trim.toUpperCase

    if (!ValidRegimes.contains(regime.trim.toLowerCase()))
      logger.error(s"[AssessmentsService][getOtherAssessments] Invalid Regime Code $reqText")
      Future.successful(Left(InvalidRegimeCode))
    else if (!regNumberPattern.matcher(regNumber).matches())
      logger.warn(s"[AssessmentsService][getOtherAssessments] Invalid pattern for regNumber=$regNumber")
      Future.successful(Left(InvalidRegNumber))
    else
      repository
        .getOtherAssessments(regNumber, paginationStart, paginationMaxRows)
        .map(assessments => Right(assessments))
        .recover { case ex: Exception =>
          logger.error(s"[AssessmentsService][getOtherAssessments] Unexpected error $reqText", ex)
          Left(UnexpectedError)
        }
  }
}
