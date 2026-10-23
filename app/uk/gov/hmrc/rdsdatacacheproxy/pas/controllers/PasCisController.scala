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

package uk.gov.hmrc.rdsdatacacheproxy.pas.controllers

import com.google.inject.Inject
import play.api.Logging
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*
import uk.gov.hmrc.rdsdatacacheproxy.pas.services.PasCisService

import scala.concurrent.{ExecutionContext, Future}

class PasCisController @Inject() (
  authorise: AuthAction,
  service: PasCisService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def createCisAppeal: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[CreateAppealRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .createCisAppeal(body)
              .map { CreateAppealResponse =>
                Created(Json.toJson(CreateAppealResponse))
              }
              .recover { case t =>
                logger.error("[createCisAppeal] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }

  def getCisAppealablePenalties: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[GetAppealablePenaltiesRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .getCisAppealablePenalties(body)
              .map(response => Ok(Json.toJson(response)))
              .recover { case t =>
                logger.error("[getCisAppealablePenalties] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }

  def getCisAppealablePenalty: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[GetAppealablePenaltyRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .getCisAppealablePenalty(body)
              .map { GetAppealablePenaltyResponse =>
                Ok(Json.toJson(GetAppealablePenaltyResponse))
              }
              .recover { case t =>
                logger.error("[getCisAppealablePenalty] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }
}
