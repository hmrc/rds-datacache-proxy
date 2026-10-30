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
import uk.gov.hmrc.rdsdatacacheproxy.pas.services.PasPayeService

import scala.concurrent.{ExecutionContext, Future}

class PasPayeController @Inject() (
  authorise: AuthAction,
  service: PasPayeService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def createPayeAppeal: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[CreateAppealRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .createPayeAppeal(body)
              .map(response => Created(Json.toJson(response)))
              .recover { case t =>
                logger.error("[createPayeAppeal] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }

  def getPayeAppealablePenalties: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[GetAppealablePenaltiesRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .getPayeAppealablePenalties(body)
              .map(response => Ok(Json.toJson(response)))
              .recover { case t =>
                logger.error("[getPayeAppealablePenalties] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }

  def getPayeAppealablePenalty: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[GetAppealablePenaltyRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .getPayeAppealablePenalty(body)
              .map { GetAppealablePenaltyResponse =>
                Ok(Json.toJson(GetAppealablePenaltyResponse))
              }
              .recover { case t =>
                logger.error("[getPayeAppealablePenalty] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }

  def getPayeTaxpayer: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[GetTaxpayerRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .getPayeTaxpayer(body)
              .map {
                case Some(response) => Ok(Json.toJson(response))
                case None           => NotFound(Json.obj("message" -> "PAYE taxpayer not found"))
              }
              .recover { case t =>
                logger.error("[getPayeTaxpayer] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }

  def getPayePenaltyAllowed: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[GetPenaltyAllowedRequest]
        .fold(
          errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .getPayePenaltyAllowed(body)
              .map(response => Ok(Json.toJson(response)))
              .recover { case t =>
                logger.error("[getPayePenaltyAllowed] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }
}
