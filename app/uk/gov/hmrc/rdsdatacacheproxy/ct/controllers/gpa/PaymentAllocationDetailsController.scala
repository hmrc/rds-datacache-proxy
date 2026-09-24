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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.gpa

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PaymentAllocationDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa.PaymentAllocationDetailsService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentAllocationDetailsController @Inject() (
  authorise: AuthAction,
  paymentAllocationDetailsService: PaymentAllocationDetailsService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getGPAPaymentAllocationDetail(gpaUtr: Long,
                                    gppContractVersion: Long,
                                    participatorUtr: Long,
                                    participatorAp: Long,
                                    startIndex: Long,
                                    count: Long
                                   ): Action[AnyContent] = authorise.async { implicit request =>
    paymentAllocationDetailsService
      .getGPAPaymentAllocationDetail(gpaUtr, gppContractVersion, participatorUtr, participatorAp, startIndex, count)
      .map { paymentAllocationDetails =>
        Ok(Json.toJson(paymentAllocationDetails))
      }
      .recover { case ex: Exception =>
        logger.error("Error while retrieving payment allocation details", ex)
        InternalServerError(Json.obj("error" -> "Failed to retrieve payment allocation details"))
      }
  }
}
