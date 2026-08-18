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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.Payments
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.PaymentsDataSource

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentsController @Inject() (authorise: AuthAction, repository: PaymentsDataSource, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with Logging {

  def getPayments(taxRef: Long, accPeriod: Long): Action[AnyContent] =
    authorise.async { implicit request =>
      repository
        .getPayments(taxRef, accPeriod)
        .map { paymentTransactions =>
          Ok(Json.toJson(Payments(paymentTransactions)))
        }
        .recover { case ex: Exception =>
          logger.error("error while retrieving payment transactions", ex)
          InternalServerError("Failed to retrieve payment transactions")
        }
    }
}
