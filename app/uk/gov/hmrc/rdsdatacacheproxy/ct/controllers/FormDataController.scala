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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CT600XmlDataResponse, FormListItem}
import uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.FormDataQueryParams
import javax.inject.Inject
import scala.concurrent.Future

class FormDataController @Inject() (
  authorise: AuthAction,
  cc: ControllerComponents
)
//(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getFormData(taxRef: Long, accPeriod: Long, queryParams: FormDataQueryParams): Action[AnyContent] = authorise.async { implicit request =>
    val dataItem = CT600XmlDataResponse(
      ct600XmlData = Some("data"),
      formList = List(
        FormListItem(
          formType = "A",
          xmlData  = "someData"
        )
      )
    )
    Future.successful(
      Ok(Json.toJson(dataItem))
    )
//    connector
//      .getIsAPBalanced(taxRef, accPeriod)
//      .map(record => Ok(Json.toJson(APBalanced(accountingPeriodDetails = record))))
//      .recover { case ex: Exception =>
//        logger.error("Error while retrieving IsAPBalanced", ex)
//        InternalServerError(Json.obj("error" -> "Failed to retrieve IsAPBalanced"))
//      }
  }
}
