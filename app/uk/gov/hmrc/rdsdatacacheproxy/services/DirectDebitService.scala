/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.services

import uk.gov.hmrc.rdsdatacacheproxy.connectors.RdsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.models.UserDebits
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.EarliestPaymentDate

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

class DirectDebitService @Inject()(rdsDatacache: RdsDataSource):

  def retrieveDirectDebits(id: String, start: Int, max: Int): Future[UserDebits] =
    rdsDatacache.getDirectDebits(id, start, max)

  def getEarliestPaymentDate(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] =
    rdsDatacache.getEarliestPaymentDate(baseDate, offsetWorkingDays)
