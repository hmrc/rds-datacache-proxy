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
import scala.concurrent.Future

@Singleton
class AgentDataSourceStub extends AgentDataSource with Logging {

  override def getAllClientsDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int): Future[Int] = {
    val credentialIdExists = Option(credentialId).exists(_.trim.nonEmpty)
    val serviceNameExists = Option(serviceName).exists(_.trim.nonEmpty)

    if (credentialIdExists && serviceNameExists) {
      logger.info(
        s"[STUB] getClientListDownloadStatus -> CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, SERVICE_NAME=${Option(serviceName).map(_.trim).getOrElse("")} => status=1"
      )
      Future.successful(1)
    } else {
      logger.warn(
        s"[STUB] getClientListDownloadStatus -> missing/blank CREDENTIAL_ID/SERVICE_NAME: CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, SERVICE_NAME=${Option(serviceName).map(_.trim).getOrElse("")} "
      )
      Future.successful(2)
    }
  }

  override def getAllClients(
    regime: Regime,
    credentialId: String,
    start: Int,
    count: Int,
    sort: Int,
    order: String
  ): Future[Either[StatementError, AgentClientListResponse]] = {
    val credentialIdExists = Option(credentialId).exists(_.trim.nonEmpty)

    if (credentialIdExists) {
      val clients = List(
        AgentClient("111222333", "Jones Motors Ltd", "123"),
        AgentClient("444555666", "Smith Supplies", "456"),
        AgentClient("555666777", "XYZ ltd", "456")
      )

      val nameChars = List("J", "S", "X")

      logger.info(
        s"[STUB] getAllClients -> regime=${regime.code}, CREDENTIAL_ID=${credentialId.trim}, START=$start, COUNT=$count => ${clients.length} clients"
      )

      Future.successful(
        Right(
          AgentClientListResponse(
            clients                      = clients,
            totalCount                   = clients.length,
            clientNameStartingCharacters = nameChars
          )
        )
      )
    } else {
      logger.warn(
        s"[STUB] getAllClients -> missing/blank IR_AGENT_ID/CREDENTIAL_ID: regime=${regime.code}, CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}"
      )
      Future.successful(
        Right(
          AgentClientListResponse(
            clients                      = List.empty,
            totalCount                   = 0,
            clientNameStartingCharacters = List.empty
          )
        )
      )
    }
  }

  override def hasClient(
    regime: Regime,
    credentialId: String,
    regNumber: String
  ): Future[Either[StatementError, Boolean]] = {
    val allPresent = List(
      credentialId,
      regNumber
    ).forall(_.trim.nonEmpty)

    if (allPresent) {
      val clientExists = regNumber.trim match {
        case "XEM00000000640" => true
        case "XVM00000000495" => true
        case "XHM00000000785" => true
        case _       => false
      }

      logger.info(
        s"[STUB] hasClient -> regime=${regime.code}, CREDENTIAL_ID=${credentialId.trim}, regNumber=${regNumber.trim} => exists=$clientExists"
      )

      Future.successful(Right(clientExists))
    } else {
      logger.warn(
        s"[STUB] hasClient -> missing/blank parameters: regime=${regime.code}, CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, regNumber=${Option(regNumber).map(_.trim).getOrElse("")}"
      )
      Future.successful(Right(false))
    }
  }
}
