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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.stub

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.agent.{AgentClient, AgentClientListResponse}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.AgentDataSource

import javax.inject.Singleton
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentRdsStub extends AgentDataSource {
  override def hasClient(regime: Regime, credentialId: String, regNumber: String) =
    Future {
      Right(false)
    }

  override def getAllClientsDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int = 14400) =
    Future(1)

  override def getAllClients(regime: Regime, credentialId: String, start: Int = 0, count: Int = -1, sort: Int = 0, order: String = "ASC") =
    Future(AgentStubs.getAgentClientListResponseData(2))
}

object AgentStubs {

  def getAgentClientListResponseData(count: Int): Either[StatementError, AgentClientListResponse] =
    count match {
      case 2 =>
        Right(
          AgentClientListResponse(
            clients = List(
              AgentClient("111222333", "Jones Motors Ltd", "123"),
              AgentClient("444555666", "Smith Supplies", "456")
            ),
            totalCount                   = 2,
            clientNameStartingCharacters = List("J", "S")
          )
        )
      case _ =>
        Right(
          AgentClientListResponse(
            clients                      = List.empty,
            totalCount                   = 0,
            clientNameStartingCharacters = List.empty
          )
        )
    }
}
