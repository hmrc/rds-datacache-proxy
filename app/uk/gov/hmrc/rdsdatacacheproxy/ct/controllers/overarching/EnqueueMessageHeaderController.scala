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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.overarching

import play.api.Logging
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.EnqueueMessageHeaderRequest
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.overarching.EnqueueMessageHeaderService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnqueueMessageHeaderController @Inject() (
                                                 authorise: AuthAction,
                                                 service: EnqueueMessageHeaderService,
                                                 cc: ControllerComponents
                                               ) (implicit ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def enqueueMessageHeader(): Action[JsValue] = authorise(parse.json).async { implicit request =>
    request.body
      .validate[EnqueueMessageHeaderRequest]
      .fold(
        errs => Future.successful(BadRequest(Json.obj("message" -> "Invalid Json", "errors" -> JsError.toJson(errs)))),
        req =>
          service
            .enqueueMessageHeader(req)
            .map { enqueueMessageHeader =>
              Ok(Json.toJson(enqueueMessageHeader))
            }
            .recover { case ex =>
              logger.error("Error while retrieving enqueueMessageHeader", ex)
              InternalServerError(Json.obj("error" -> "Failed to retrieve enqueueMessageHeader"))
            }
      )
  }

}
