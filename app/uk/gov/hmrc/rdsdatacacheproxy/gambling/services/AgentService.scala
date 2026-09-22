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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.{AgentClientListResponse, ClientListDownloadStatus}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.AgentDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentService @Inject() (
  repository: AgentDataSource
)(implicit ec: ExecutionContext)
    extends BaseService {

  def getAllClientsDownloadStatus(credentialId: String, regime: String, gracePeriod: Int = 14400)(using
    ExecutionContext
  ): Future[Either[String, ClientListDownloadStatus]] = {
    repository
      .getAllClientsDownloadStatus(credentialId, regime, gracePeriod)
      .map(ClientListDownloadStatus.fromInt)
  }

  def getAllClients(
    regime: String,
    credentialId: String,
    start: Int,
    count: Int,
    sort: Int,
    ascending: Boolean
  )(implicit hc: HeaderCarrier): Future[Either[StatementError, AgentClientListResponse]] = {
    val order = if (ascending) "ASC" else "DESC"
    withValidAgentParams(regime, credentialId, start, count, sort, order, "[getClientList]")(repository.getAllClients)
  }

  def hasClient(
    regime: String,
    credentialId: String,
    regNumber: String
  ): Future[Either[StatementError, Boolean]] = {
    withValidAgentParams(regime, credentialId, regNumber, "[hasClient]")(repository.hasClient)
  }
}
