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
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.AdministrativeRuleService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GetAdministrativeRuleController @Inject() (
  val cc: ControllerComponents,
  authorise: AuthAction,
  service: AdministrativeRuleService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with I18nSupport
    with Logging {

  def getAdministrativeRule(adminRuleKey: String): Action[AnyContent] = {
    authorise.async { request =>
      service
        .getAdminRule(adminRuleKey)
        .map { adminRule =>
          Ok(Json.toJson(adminRule))
        }
        .recover { case ex: Exception =>
          logger.error("Unexpected Exception", ex)
          InternalServerError(Json.obj("message" -> "Unable to Retrieve adminRule"))
        }
    }
  }

}
