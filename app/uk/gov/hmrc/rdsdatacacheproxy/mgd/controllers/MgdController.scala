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

package uk.gov.hmrc.rdsdatacacheproxy.mgd.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.MgdError.*
import uk.gov.hmrc.rdsdatacacheproxy.mgd.services.MgdService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MgdController @Inject() (
  authorise: AuthAction,
  service: MgdService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getReturnSummary(mgdRegNumber: String): Action[AnyContent] =
    authorise.async { implicit request =>

      service.getReturnSummary(mgdRegNumber.trim).map {
        case Right(summary) =>
          Ok(Json.toJson(summary))

        case Left(InvalidMgdRegNumber) =>
          BadRequest(Json.obj("error" -> InvalidMgdRegNumber.message))

        case Left(NotFound) =>
          NotFound(Json.obj("error" -> ReturnSummaryNotFound.message))

        case Left(UpstreamError(status)) =>
          BadGateway(Json.obj("error" -> s"Upstream error: $status"))

        case Left(UnexpectedError) =>
          InternalServerError(Json.obj("error" -> UnexpectedError.message))
      }
    }
}
