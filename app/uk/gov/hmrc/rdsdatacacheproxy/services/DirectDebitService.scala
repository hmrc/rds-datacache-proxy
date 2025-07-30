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

import uk.gov.hmrc.rdsdatacacheproxy.connectors.RDSConnector
import uk.gov.hmrc.rdsdatacacheproxy.models.UserDebits

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitService @Inject()(rdsConnector: RDSConnector)
                                  (implicit ec: ExecutionContext):

  def retrieveDirectDebits(id: String, start: Int, max: Int): Future[UserDebits] =
    rdsConnector.getDirectDebits(id, start, max) map { debits =>
      UserDebits(debits.size, debits)
    }
