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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers.testOnly

import play.api.Logging
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers.BaseController
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.UpdateStatusPeriodService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SetPeriodStatusController @Inject() (service: UpdateStatusPeriodService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with BaseController
    with Logging {

  def setPeriodStatus(regNumber: String, consecNo: Int, status: Int): Action[AnyContent] =
    Action.async { implicit request =>
      service.updateStatusPeriod(Regime.MGD.code, regNumber, consecNo, status).map {
        case Right(())   => Ok(s"Period status completed for Regime=MGD / RegNumber=$regNumber / ConsecNo=$consecNo / STATUS=$status")
        case Left(error) => handleError(error)
      }
    }
}
