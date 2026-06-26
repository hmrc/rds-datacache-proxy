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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.StatementOverview
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.StatementOverviewDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StatementOverviewService @Inject() (
  repository: StatementOverviewDataSource
)(implicit ec: ExecutionContext)
    extends BaseService {

  def getStatementOverview(regime: String, rawRegNumber: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[StatementError, StatementOverview]] =
    withValidParams(regime, rawRegNumber.trim.toUpperCase, "getStatementOverview")(
      repository.getStatementOverview
    ) // .map(a => a)

  // ALL OF THE BELOW MIGHT NOT BE NEEDED - see "RESULTS OF TESTING" below
//      .map {
//      case Right(Some(overview)) => Right(overview)
//      case Right(None)           => Left(StatementNotFound)
//      case Left(error)           => Left(error)
//    }

  /*  RESULTS OF TESTING

curl http://localhost:6992/rds-datacache-proxy/gambling/statement-overview/gbd/XAM00003402079
{"gtrPeriodStartDate":"2013-10-01","gtrPeriodEndDate":"2014-12-31","total":-1439.4,"balance":-1350,"amountDeclared":0,"assessments":0,"penalties":0,"adjustments":0,"reallocations":0,"otherAssessments":0,"interest":-89.4,"payments":0}

curl http://localhost:6992/rds-datacache-proxy/gambling/statement-overview/gbd/XAM0000999999
{"code":"INVALID_REG_NUMBER","message":"regNumber has invalid format"}

curl http://localhost:6992/rds-datacache-proxy/gambling/statement-overview/gbd/XAM00009999999
{"code":"NOT_FOUND","message":"No statement overview found for the given registration number"}


   */
}
