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

package uk.gov.hmrc.rdsdatacacheproxy.cis.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.ClientService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClientController @Inject() (
  authorise: AuthAction,
  clientService: ClientService,
  cc: ControllerComponents
)(using ExecutionContext)
    extends BackendController(cc)
    with Logging {
  def getClientListDownloadStatus(
    credentialId: String,
    serviceName: String,
    gracePeriod: Int = 14400
  ): Action[AnyContent] = authorise.async { implicit request =>

    if (serviceName.trim().isEmpty || credentialId.trim().isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "credentialId and serviceName must be provided")))
    } else {
      clientService
        .getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
        .map {
          case Left(error)   => InternalServerError(Json.obj("error" -> error))
          case Right(status) => Ok(Json.obj("status" -> status.toString))
        }
    }
  }
}
