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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingReturnsError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.GamblingReturnsError.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.GamblingReturnsService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GamblingReturnsController @Inject() (authorise: AuthAction, service: GamblingReturnsService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with Logging {

  def getReturnsSubmitted(regime: String, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Action[AnyContent] =
    authorise.async { implicit request =>
      service.getReturnsSubmitted(regime, regNumber, paginationStart, paginationMaxRows).map {
        case Right(returns) => Ok(Json.toJson(returns))
        case Left(error) =>
          error match {
            case InvalidRegimeCode | InvalidRegNumber | RegNumberNotFound =>
              BadRequest(errorResponse(error))
            case UnexpectedError =>
              InternalServerError(errorResponse(error))
          }
      }
    }
}
