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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.GroupSummaryDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.GpaPaymentDetailsQueryParams
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.GroupPaymentsRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GroupPaymentsController @Inject() (authorise: AuthAction, groupPaymentsRepository: GroupPaymentsRepository, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with Logging {

  def getGroupSummary(gpaUTR: Long, nomCompanyUTR: Long): Action[AnyContent] =
    authorise.async { request =>
      groupPaymentsRepository
        .getGroupSummary(
          gpaUTR,
          nomCompanyUTR
        )
        .map {
          case Some(gpaRecord) =>
            Ok(Json.toJson(gpaRecord))
          case None =>
            NotFound(Json.toJson(None))
        }
        .recover { case ex: Exception =>
          logger.error("error while retrieving group payments", ex)
          InternalServerError("Failed to retrieve group payments")
        }
    }

  def getPaymentDetails(gpaUTR: Long, queryParams: GpaPaymentDetailsQueryParams): Action[AnyContent] = {
    authorise.async { request =>
      groupPaymentsRepository
        .getPaymentsDetails(
          gpaUTR,
          queryParams.contractVersion,
          queryParams.startIndex,
          queryParams.count
        )
        .map { gpaPayDetails =>
          Ok(Json.toJson(gpaPayDetails))
        }
        .recover { case ex: Exception =>
          logger.error("error while retrieving group payments details", ex)
          InternalServerError("Failed to retrieve group payments details")
        }

    }

  }

}
