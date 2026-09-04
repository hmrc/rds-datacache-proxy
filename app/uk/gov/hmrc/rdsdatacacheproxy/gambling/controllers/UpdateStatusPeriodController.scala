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
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.UpdateStatusPeriodRequest
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.UpdateStatusPeriodService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UpdateStatusPeriodController @Inject() (authorise: AuthAction, service: UpdateStatusPeriodService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with BaseController
    with Logging {

  def updateStatusPeriod(regime: String, regNumber: String, periodId: Int): Action[UpdateStatusPeriodRequest] =
    authorise.async(parse.json[UpdateStatusPeriodRequest]) { implicit request =>
      service.updateStatusPeriod(regime, regNumber, periodId, request.body.status).map {
        case Right(())   => NoContent
        case Left(error) => handleError(error)
      }
    }
}
