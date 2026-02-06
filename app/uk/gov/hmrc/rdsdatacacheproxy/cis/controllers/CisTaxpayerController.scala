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
import play.api.mvc.Results.{InternalServerError, NotFound, Status}
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.EmployerReference
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.CisTaxpayerService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisTaxpayerController @Inject() (
  authorise: AuthAction,
  service: CisTaxpayerService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {
  def getCisTaxpayerByTaxReference: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[EmployerReference]
        .fold(
          errs =>
            Future.successful(
              BadRequest(
                Json.obj(
                  "message" -> "Invalid JSON body",
                  "errors"  -> JsError.toJson(errs)
                )
              )
            ),
          er =>
            service
              .getCisTaxpayerByTaxReference(er.taxOfficeNumber, er.taxOfficeReference)
              .map(tp => Ok(Json.toJson(tp)))
              .recover(recoverServiceErrors("getCisTaxpayerByTaxReference", er))
        )
    }

  private def recoverServiceErrors(
    op: String,
    er: EmployerReference
  ): PartialFunction[Throwable, Result] = {
    case u: UpstreamErrorResponse =>
      Status(u.statusCode)(Json.obj("message" -> u.message))

    case _: NoSuchElementException =>
      NotFound(
        Json.obj(
          "message" -> s"CIS taxpayer not found for TON=${er.taxOfficeNumber}, TOR=${er.taxOfficeReference}"
        )
      )

    case t: Throwable =>
      logger.error(s"[$op] failed for TON=${er.taxOfficeNumber}, TOR=${er.taxOfficeReference}", t)
      InternalServerError(Json.obj("message" -> "Unexpected error"))
  }
}
