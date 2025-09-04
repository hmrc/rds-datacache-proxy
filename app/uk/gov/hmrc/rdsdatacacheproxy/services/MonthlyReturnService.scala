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
import uk.gov.hmrc.rdsdatacacheproxy.connectors.CisMonthlyReturnSource
import uk.gov.hmrc.rdsdatacacheproxy.models.UserMonthlyReturns

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MonthlyReturnService @Inject()(cisSource: CisMonthlyReturnSource)
                                    (implicit ec: ExecutionContext) extends Logging:

  def retrieveMonthlyReturns(taxOfficeNumber: String, taxOfficeReference: String)
  : Future[UserMonthlyReturns] = {
    cisSource.findInstanceId(taxOfficeNumber, taxOfficeReference).flatMap {
      case Some(instanceId) =>
        cisSource.getMonthlyReturns(instanceId)
      case None =>
        val msg = s"No instanceId found for TON=$taxOfficeNumber, TOR=$taxOfficeReference"
        logger.warn(msg)
        Future.failed(new NoSuchElementException(msg))
    }
  }


