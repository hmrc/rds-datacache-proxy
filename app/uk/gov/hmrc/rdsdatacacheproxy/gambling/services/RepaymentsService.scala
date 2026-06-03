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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{ActualRepayments, Regime, RepaymentsSummary}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepaymentsDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepaymentsService @Inject() (
  repository: RepaymentsDataSource
)(implicit ec: ExecutionContext)
    extends BaseService {

  def getRepaymentsSummary(regime: String, rawRegNumber: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[StatementError, RepaymentsSummary]] =
    withValidParams(regime, rawRegNumber.trim.toUpperCase, "getRepaymentsSummary")(repository.getRepaymentsSummary)

  def getActualRepayments(regime: String, rawRegNumber: String, paginationStart: Int, paginationMaxRows: Int)(implicit
    hc: HeaderCarrier
  ): Future[Either[StatementError, ActualRepayments]] =
    withValidParams(regime, rawRegNumber.trim.toUpperCase, paginationStart, paginationMaxRows, "getActualRepayments")(repository.getActualRepayments)

}
