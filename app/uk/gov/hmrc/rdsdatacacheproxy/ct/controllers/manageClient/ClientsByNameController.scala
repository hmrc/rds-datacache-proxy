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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.manageClient

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.ClientsByName
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.manageClient.ClientsByNameService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ClientsByNameController @Inject() (
                                       authorise: AuthAction,
                                       clientsByNameService: ClientsByNameService,
                                       cc: ControllerComponents
                                     )(implicit ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def getClientsByName(irAgentId: String, credentialId: String, name: String, start: Int, count: Int, sort: Int, order: String): Action[AnyContent] = authorise.async {
    implicit request =>
      clientsByNameService
        .getClientsByName(irAgentId, credentialId, name, start, count, sort, order)
        .map { clientsByName =>
          Ok(Json.toJson(clientsByName))
        }
        .recover { case ex: Exception =>
          logger.error("Error while retrieving clients by namme", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve clients by name"))
        }
  }
}
