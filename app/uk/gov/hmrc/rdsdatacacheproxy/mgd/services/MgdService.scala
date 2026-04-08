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

package uk.gov.hmrc.rdsdatacacheproxy.mgd.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.*
import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.MgdError.*
import uk.gov.hmrc.rdsdatacacheproxy.mgd.repositories.MgdDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MgdService @Inject() (
  mgdDataSourceRepository: MgdDataSource
)(implicit ec: ExecutionContext) {

  def getReturnSummary(
    mgdRegNumber: String
  )(implicit hc: HeaderCarrier): Future[Either[MgdError, ReturnSummary]] = {

    if (mgdRegNumber.isEmpty) {
      Future.successful(Left(InvalidMgdRegNumber))
    } else {

      mgdDataSourceRepository
        .getReturnSummary(mgdRegNumber)
        .map {
          case Some(summary) => Right(summary)
          case None          => Left(ReturnSummaryNotFound)
        }
        .recover { case ex =>
          Left(UnexpectedError)
        }
    }
  }
}
