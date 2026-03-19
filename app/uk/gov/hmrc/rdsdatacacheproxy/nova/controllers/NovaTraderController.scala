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

package uk.gov.hmrc.rdsdatacacheproxy.nova.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.nova.repositories.NovaDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NovaTraderController @Inject() (
  authorise: AuthAction,
  novaDataSource: NovaDataSource,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getTraderDetails(userVrn: String, clientVrn: Option[String]): Action[AnyContent] = authorise.async { implicit request =>
    if (userVrn.trim.isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "userVrn must be provided")))
    } else {
      novaDataSource
        .getTraderDetails(userVrn, clientVrn.map(_.trim).filter(_.nonEmpty))
        .map {
          case Some(details) => Ok(Json.toJson(details))
          case None          => NotFound
        }
        .recover { case ex: Exception =>
          logger.error(s"[NOVA] Error retrieving trader details for userVrn=$userVrn", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve trader details"))
        }
    }
  }
}
