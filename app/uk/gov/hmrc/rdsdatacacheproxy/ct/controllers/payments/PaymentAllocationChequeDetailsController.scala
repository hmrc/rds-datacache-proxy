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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.payments

import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentAllocationChequeDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments.PaymentAllocationChequeDetailsService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentAllocationChequeDetailsController @Inject (
  cc: ControllerComponents,
  authorise: AuthAction,
  service: PaymentAllocationChequeDetailsService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with I18nSupport {

  def getPaymentAllocationChequeDetails(taxRef: Long, chequeId: Long): Action[AnyContent] = {
    authorise.async { implicit request =>
      service
        .getPaymentAllocationChequeDetails(taxRef, chequeId)
        .map { (value: PaymentAllocationChequeDetails) =>
          Ok(Json.toJson(value))
        }
        .recover { case ex: Throwable =>
          logger.error("Error while retrieving PaymentAllocationChequeDetails from oracle database", ex)
          InternalServerError(Json.obj("message" -> "Unexpected error"))
        }

    }
  }
}
