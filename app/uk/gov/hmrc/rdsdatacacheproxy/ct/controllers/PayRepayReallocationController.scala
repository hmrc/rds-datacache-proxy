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
import play.api.libs.json.Json
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PayRepayReallocations
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.PayRepayReallocationService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PayRepayReallocationController @Inject() (
  authorise: AuthAction,
  payRepayReallocationService: PayRepayReallocationService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getTotalAmounts(taxRef: Long, accPeriod: Long): Action[AnyContent] = authorise.async { implicit request =>
    payRepayReallocationService
      .getTotalAmounts(taxRef, accPeriod)
      .map { payRepayReallocations =>
        Ok(Json.toJson(payRepayReallocations))
      }
      .recover { case ex: Exception =>
        logger.error("Error while retrieving the payment repayment allocations", ex)
        InternalServerError(Json.obj("error" -> "Failed to retrieve the payment repayment allocations"))
      }
  }
}
