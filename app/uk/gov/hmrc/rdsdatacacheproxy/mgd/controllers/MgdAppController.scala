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

package uk.gov.hmrc.rdsdatacacheproxy.mgd.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.ReturnSummary
import uk.gov.hmrc.rdsdatacacheproxy.mgd.repositories.MgdDataSource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MgdAppController @Inject() (
                                       authorise: AuthAction,
                                       mgdDataSource: MgdDataSource,
                                       cc: ControllerComponents
                                     )(implicit ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def getTraderDetails(mgd_reg_number: String, returns_due: Int, returns_overdue: Int): Action[AnyContent] = authorise.async { implicit request =>
    if (mgd_reg_number.trim.isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "mgd_reg_number must be provided")))
    } else {
      mgdDataSource
        .getReturnSummary(mgd_reg_number, returns_due, returns_overdue)
        .map {
          case Some(details) => Ok(Json.toJson(details))
          case None          => NotFound
        }
        .recover { case ex: Exception =>
          logger.error(s"[MGD] Error retrieving details for mgd_reg_number=$mgd_reg_number", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve details"))
        }
    }
  }
}