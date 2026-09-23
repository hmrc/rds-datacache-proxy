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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.gambling.config.AppConfig
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.AgentService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentController @Inject (appConfig: AppConfig)(authorise: AuthAction, service: AgentService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with BaseController
    with Logging {

  def getClientListDownloadStatus(
    credentialId: String,
    regime: String,
    gracePeriod: Int = appConfig.gracePeriod
  ): Action[AnyContent] = authorise.async { implicit request =>

    if (regime.trim().isEmpty || credentialId.trim().isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "credentialId and regime must be provided")))
    } else {
      service.getAllClientsDownloadStatus(credentialId, regime, gracePeriod).map {
        case Right(status) => Ok(Json.obj("status" -> status.toString))
        case Left(error)   => handleError(error)
      }
    }
  }

  def getAllClients(
    credentialId: String,
    regime: String,
    start: Int = 0,
    count: Int = -1,
    sort: Int = 0,
    ascending: Boolean = true
  ): Action[AnyContent] = authorise.async { implicit request =>

    if (credentialId.trim().isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "credentialId must be provided")))
    } else {
      service.getAllClients(regime, credentialId, start, count, sort, ascending).map {
        case Right(result) => Ok(Json.toJson(result))
        case Left(error)   => handleError(error)
      }
    }
  }

  def hasClient(
    regime: String,
    regNumber: String,
    credentialId: String
  ): Action[AnyContent] = authorise.async { implicit request =>
    if (credentialId.trim().isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "credentialId must be provided")))
    } else {
      service.hasClient(regime, credentialId, regNumber).map {
        case Right(exists) => Ok(Json.obj("hasClient" -> exists))
        case Left(error)   => handleError(error)
      }
    }
  }
}
