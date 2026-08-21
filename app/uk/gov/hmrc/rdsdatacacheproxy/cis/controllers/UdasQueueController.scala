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
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{EnqueueClobRequest, EnqueueMessageHeaderRequest}
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.UdasQueueService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UdasQueueController @Inject() (
  authorise: AuthAction,
  service: UdasQueueService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {
  def enqueueMessageHeader(): Action[JsValue] = authorise(parse.json).async { implicit request =>
    request.body
      .validate[EnqueueMessageHeaderRequest]
      .fold(
        errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
        req =>
          service
            .enqueueMessageHeader(req)
            .map(id => Ok(Json.obj("messageId" -> id)))
            .recover { case ex =>
              logger.error("[enqueueMessageHeader] failed", ex)
              InternalServerError(Json.obj("message" -> "Unexpected error"))
            }
      )
  }

  def enqueueClob(): Action[JsValue] = authorise(parse.json).async { implicit request =>
    request.body
      .validate[EnqueueClobRequest]
      .fold(
        errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
        req =>
          service
            .enqueueClob(req)
            .map(id => Ok(Json.obj("messageIDOut" -> id)))
            .recover { case ex =>
              logger.error("[enqueueClob] failed", ex)
              InternalServerError(Json.obj("message" -> "Unexpected error"))
            }
      )
  }
}
