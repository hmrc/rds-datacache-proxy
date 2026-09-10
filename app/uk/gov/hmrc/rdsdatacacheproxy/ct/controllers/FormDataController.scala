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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CT600XmlDataResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.FormDataQueryParams
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.FormDataRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FormDataController @Inject() (
  authorise: AuthAction,
  formDataRepository: FormDataRepository,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getFormData(taxRef: Long, accPeriod: Long, queryParams: FormDataQueryParams): Action[AnyContent] = authorise.async { implicit request =>
    formDataRepository
      .getData(taxRef = taxRef, accPeriod = accPeriod, startDate = queryParams.startDate, endDate = queryParams.endDate)
      .map(response => Ok(Json.toJson(response)))
      .recover { case ex: Exception =>
        logger.error("Error while retrieving FormData", ex)
        InternalServerError(Json.obj("error" -> "Failed to retrieve FormData"))
      }
  }
}
