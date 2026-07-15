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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.TaxTransactions
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.TaxTransactionsService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TaxTransactionsController @Inject() (service: TaxTransactionsService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with Logging {

  def getTaxTransactions(taxRef: Long, accPeriod: Long): Action[AnyContent] =
    Action.async { implicit request =>
      service
        .getTaxTransactions(taxRef, accPeriod)
        .map { taxTransactions =>
          Ok(Json.toJson(TaxTransactions(taxTransactions)))
        }
        .recover { case ex: Exception =>
          logger.error("error while retrieving tax transactions", ex)
          InternalServerError("Failed to retrieve tax transactions")
        }
    }
}
