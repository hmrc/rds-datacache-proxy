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

import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentSearchResultResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.payments.PaymentSearchResultQueryParams
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PaymentSearchResultRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentSearchResultController @Inject() (
  cc: ControllerComponents,
  authorise: AuthAction,
  repository: PaymentSearchResultRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with I18nSupport
    with Logging {

  def getPaymentSearchResult(taxRef: Long, queryParams: PaymentSearchResultQueryParams): Action[AnyContent] =
    authorise.async { implicit request =>
      repository
        .getPaymentSearchResult(taxRef, fromDate = queryParams.fromDate, toDate = queryParams.toDate)
        .map(payload => Ok(Json.toJson(payload)))
        .recover { case ex: Throwable =>
          logger.error("Error while retrieving PaymentSearchResultResponse from oracle database", ex)
          InternalServerError(Json.obj("message" -> "Unexpected error"))
        }

    }

}
