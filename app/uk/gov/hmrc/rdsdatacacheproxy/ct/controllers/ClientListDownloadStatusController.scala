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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.ClientListDownloadStatusService
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ClientDownloadStatus

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ClientListDownloadStatusController @Inject() (
  authorise: AuthAction,
  service: ClientListDownloadStatusService,
  cc: ControllerComponents
)(using ExecutionContext)
    extends BackendController(cc)
    with Logging {
  def getClientListDownloadStatus(
    credentialId: String,
    serviceName: String,
    gracePeriod: Int
  ): Action[AnyContent] = authorise.async { implicit request =>
    service
      .getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
      .map(status => Ok(Json.toJson(status)))
      .recover { case ex: Exception =>
        logger.error("Error while retrieving client list download status", ex)
        InternalServerError(Json.obj("message" -> "Unexpected error"))
      }
  }
}
