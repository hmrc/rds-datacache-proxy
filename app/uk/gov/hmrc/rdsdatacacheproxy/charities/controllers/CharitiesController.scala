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

package uk.gov.hmrc.rdsdatacacheproxy.charities.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.charities.models.{AgentNameResponse, OrganisationNameResponse}
import uk.gov.hmrc.rdsdatacacheproxy.charities.repositories.CharitiesDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CharitiesController @Inject() (
  authorise: AuthAction,
  charitiesDataSource: CharitiesDataSource,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getAgentName(agentRef: String): Action[AnyContent] = authorise.async { implicit request =>
    if (agentRef.trim().isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "agentRef must be provided")))
    } else {
      charitiesDataSource
        .getAgentName(agentRef)
        .map {
          case Some(agentName) => Ok(Json.toJson(AgentNameResponse(agentName)))
          case None            => NotFound
        }
        .recover { case ex: Exception =>
          logger.error("Error while retrieving agent name from oracle database", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve agent name"))
        }
    }
  }

  def getOrganisationName(charityRef: String): Action[AnyContent] = authorise.async { implicit request =>
    if (charityRef.trim().isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "charityRef must be provided")))
    } else {
      charitiesDataSource
        .getOrganisationName(charityRef)
        .map {
          case Some(organisationName) => Ok(Json.toJson(OrganisationNameResponse(organisationName)))
          case None                   => NotFound
        }
        .recover { case ex: Exception =>
          logger.error("Error while retrieving organisation name from oracle database", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve organisation name"))
        }
    }
  }
}
