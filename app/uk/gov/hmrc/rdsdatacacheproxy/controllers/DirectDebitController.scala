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

package uk.gov.hmrc.rdsdatacacheproxy.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.CreateDirectDebitRequest
import uk.gov.hmrc.rdsdatacacheproxy.models.DirectDebit.*
import uk.gov.hmrc.rdsdatacacheproxy.services.DirectDebitService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitController @Inject()(
  authorise: AuthAction,
  directDebitService: DirectDebitService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext) extends BackendController(cc):

  def retrieveDirectDebits(offsetOption: Option[String], limitOption: Option[Int]): Action[AnyContent] =
    authorise.async:
      implicit request =>
        val id = request.internalId
        (offsetOption, limitOption) match
          case (None, None) =>
            directDebitService.retrieveDirectDebits(id).map(s => Ok(Json.toJson(s)))
          case (Some(offset), Some(limit)) =>
            directDebitService.retrieveDirectDebitsWithOffset(id, offset, limit).map(s => Ok(Json.toJson(s)))
          case (Some(_), _) =>
            Future.successful(BadRequest("Cannot provide an offset without a limit"))
          case _ =>
            Future.successful(BadRequest("Cannot provide a limit without an offset"))


  def createDirectDebit(): Action[CreateDirectDebitRequest] =
    authorise.async(parse.json[CreateDirectDebitRequest]):
      implicit request =>
        Future.successful(Created(request.body.paymentReference))
