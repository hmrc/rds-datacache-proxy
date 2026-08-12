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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.SubmittedReturnSingle
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.SubmittedReturnSingleDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmittedReturnSingleService @Inject() (
  repository: SubmittedReturnSingleDataSource
)(implicit ec: ExecutionContext)
    extends BaseService {

  def getSubmittedReturnSingle(rawRegNumber: String, consecNo: Int)(implicit
    hc: HeaderCarrier
  ): Future[Either[StatementError, SubmittedReturnSingle]] =
    withValidParams(rawRegNumber.trim.toUpperCase, consecNo, "getSubmittedReturnSingle")(repository.getSubmittedReturnSingle)
}

/* TESTED OK

curl http://localhost:6992/rds-datacache-proxy/gambling/submitted-return-details/XGM00000001201/1

{"consecNo":1,"mgdPeriod":"01/02/2013 - 31/05/2013","submittedDate":"2012-12-10","ackRef":"Not applicable","noOfMachines":33,"netTakingsHigherRate":0,"netTakingsStdRate":1500,"netTakingsLowerRate":1200,"totalDueHigherRate":0,"totalDueStdRate":1400,"totalDueLowerRate":1100,"dutyPayable":2500,"underDeclaredDuty":0,"previousReturnAmount":0,"negativeAmountCarriedForward":0,"totalNetDutyPayable":2500}


curl http://localhost:6992/rds-datacache-proxy/gambling/submitted-return-details/XGM00000009999/1

{"code":"NOT_FOUND","message":"No record found for the given registration number"}


 */
