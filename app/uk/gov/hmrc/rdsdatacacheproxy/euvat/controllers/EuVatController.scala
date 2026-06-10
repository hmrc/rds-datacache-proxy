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

package uk.gov.hmrc.rdsdatacacheproxy.euvat.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.euvat.actions.EuVatAuthAction
import uk.gov.hmrc.rdsdatacacheproxy.euvat.services.EuVatService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EuVatController @Inject() (authorise: EuVatAuthAction, euVatService: EuVatService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def retrieveTraderByVrn: Action[AnyContent] =
    authorise.async:
      implicit request =>
        val vrn = request.identifierValue
        euVatService
          .retrieveTraderByVrn(vrn)
          .map {
            case Some(trader) =>
              logger.info(s"Trader found for VRN: $vrn")
              Ok(Json.toJson(trader))
            case None =>
              logger.warn(s"No trader known facts found for VRN: $vrn")
              NotFound(Json.obj("message" -> s"No trader found for VRN $vrn"))
          }
          .recover { case ex: Exception =>
            logger.error("Error while retrieving traders known facts from oracle database", ex)
            InternalServerError("Failed to retrieve traders known facts")
          }

}
