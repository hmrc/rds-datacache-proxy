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

package uk.gov.hmrc.rdsdatacacheproxy.charities.repositories

import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CharitiesStub @Inject() () extends CharitiesDataSource with Logging:

  def getAgentName(agentRef: String): Future[Option[String]] = {
    logger.info(s"[CHARITIES-STUB] getAgentName -> agentRef: $agentRef")

    val result = agentRef match {
      case "NOT_FOUND" | "" => None
      case _                => Some("John Doe")
    }

    Future.successful(result)
  }

  def getOrganisationName(charityRef: String): Future[Option[String]] = {
    logger.info(s"[CHARITIES-STUB] getOrganisationName -> charityRef: $charityRef")

    val result = charityRef match {
      case "NOT_FOUND" | "" => None
      case _                => Some("ABC Ltd")
    }

    Future.successful(result)
  }
