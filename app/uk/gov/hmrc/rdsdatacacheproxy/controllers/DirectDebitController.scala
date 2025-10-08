/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.UserDebits.*
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.{GenerateDdiRefRequest, PaymentPlanDuplicateCheckRequest, WorkingDaysOffsetRequest}
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{EarliestPaymentDate, UserDebits}
import uk.gov.hmrc.rdsdatacacheproxy.services.DirectDebitService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DirectDebitController @Inject() (
  authorise: AuthAction,
  directDebitService: DirectDebitService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging:

  def retrieveDirectDebits(): Action[AnyContent] =
    authorise.async:
      implicit request =>
        directDebitService
          .retrieveDirectDebits(request.credentialId)
          .map(result => Ok(Json.toJson(result)))
          .recover { case ex: Exception =>
            logger.error("Error while retrieving data from oracle database", ex)
            InternalServerError("Failed to retrieve earliest data from oracle database.")
          }

  def workingDaysOffset(): Action[WorkingDaysOffsetRequest] =
    Action(parse.json[WorkingDaysOffsetRequest]).async:
      implicit request =>
        directDebitService
          .addFutureWorkingDays(request.body.baseDate, request.body.offsetWorkingDays)
          .map { result =>
            Ok(Json.toJson(result))
          }
          .recover { case ex: Exception =>
            logger.error("Error while calculating earliest payment date", ex)
            InternalServerError("Failed to calculate earliest payment date.")
          }

  def generateDDIReference(): Action[GenerateDdiRefRequest] =
    authorise.async(parse.json[GenerateDdiRefRequest]) { implicit request =>
      val body = request.body
      directDebitService
        .getDDIReference(
          body.paymentReference,
          request.credentialId,
          request.sessionId.value
        )
        .map(ddiReference => Ok(Json.toJson(ddiReference)))
        .recover { case ex: Exception =>
          logger.error("Error while generating DDI Reference", ex)
          InternalServerError("Failed to generate DDI Reference.")
        }
    }

  def retrieveDirectDebitPaymentPlans(directDebitReference: String): Action[AnyContent] =
    authorise.async:
      implicit request =>
        directDebitService
          .getDirectDebitPaymentPlans(directDebitReference, request.credentialId)
          .map(result => Ok(Json.toJson(result)))
          .recover { case ex: Exception =>
            logger.error("Error while retrieving data from oracle database", ex)
            InternalServerError("Failed to retrieve earliest data from oracle database.")
          }

  def retrievePaymentPlanDetails(directDebitReference: String, paymentPlanReference: String): Action[AnyContent] =
    authorise.async:
      implicit request =>
        directDebitService
          .getPaymentPlanDetails(directDebitReference, request.credentialId, paymentPlanReference)
          .map(result => Ok(Json.toJson(result)))
          .recover { case ex: Exception =>
            logger.error("Error while retrieving data from oracle database", ex)
            InternalServerError("Failed to retrieve earliest data from oracle database.")
          }

  def isDuplicatePaymentPlan(directDebitReference: String): Action[PaymentPlanDuplicateCheckRequest] =
    authorise.async(parse.json[PaymentPlanDuplicateCheckRequest]):
      implicit request =>
        val body = request.body
        directDebitService.isDuplicatePaymentPlan(
            directDebitReference,
            request.credentialId,
            body
          )
          .map(result => Ok(Json.toJson(result)))
          .recover {
            case ex: Exception =>
              logger.error("Error while retrieving data from oracle database", ex)
              InternalServerError("Failed to retrieve earliest data from oracle database.")
          }
