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

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.repositories.RdsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDIReference, EarliestPaymentDate, UserDebits}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

class DirectDebitService @Inject()(rdsDatacache: RdsDataSource) extends Logging:

  def retrieveDirectDebits(id: String): Future[UserDebits] =
    rdsDatacache.getDirectDebits(id)

  def addFutureWorkingDays(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] =
    rdsDatacache.addFutureWorkingDays(baseDate, offsetWorkingDays)

  def getDDIReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference] =
    rdsDatacache.getDirectDebitReference(paymentReference, credId, sessionId)
