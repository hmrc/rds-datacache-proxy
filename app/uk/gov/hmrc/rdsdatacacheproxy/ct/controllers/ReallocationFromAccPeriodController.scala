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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ReallocationFromAccPeriod
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.ReallocationFromAccPeriodService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReallocationFromAccPeriodController @Inject() (
  cc: ControllerComponents,
  authorise: AuthAction,
  service: ReallocationFromAccPeriodService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with I18nSupport
    with Logging {

  def getReallocationFromAccPeriod(taxPayerReference: Long, accountingPeriod: Long): Action[AnyContent] =
    authorise.async { implicit request =>
      service
        .getReallocationFromAccPeriod(taxPayerReference, accountingPeriod)
        .map((payload: ReallocationFromAccPeriod) => Ok(Json.toJson(payload)))
        .recover { case ex: Throwable =>
          logger.error(
            "[ReallocationFromAccPeriodController][ReallocationFromAccPeriodController] Error while retrieving ReallocationFromAccPeriod from oracle database",
            ex
          )
          InternalServerError(Json.obj("message" -> "Unexpected error"))
        }

    }

}
