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

package uk.gov.hmrc.rdsdatacacheproxy.ndds.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses.UserDebits.*
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.requests.{GenerateDdiRefRequest, PaymentPlanDuplicateCheckRequest, WorkingDaysOffsetRequest}
import uk.gov.hmrc.rdsdatacacheproxy.ndds.models.responses.{EarliestPaymentDate, UserDebits}
import uk.gov.hmrc.rdsdatacacheproxy.ndds.services.DirectDebitService

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
            InternalServerError("Failed to retrieve direct debits")
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
            logger.error("Error while calculating future working days", ex)
            InternalServerError("Failed to retrieve future working days")
          }

  def generateDDIReference(): Action[GenerateDdiRefRequest] =
    authorise.async(parse.json[GenerateDdiRefRequest]) { implicit request =>
      directDebitService
        .getDDIReference(
          request.body.paymentReference,
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
            logger.error("Error while retrieving direct debit payment plans", ex)
            InternalServerError("Failed to retrieve direct debit payment plans")
          }

  def retrievePaymentPlanDetails(directDebitReference: String, paymentPlanReference: String): Action[AnyContent] =
    authorise.async:
      implicit request =>
        directDebitService
          .getPaymentPlanDetails(directDebitReference, request.credentialId, paymentPlanReference)
          .map(result => Ok(Json.toJson(result)))
          .recover { case ex: Exception =>
            logger.error("Error while retrieving payment plan details", ex)
            InternalServerError("Failed to retrieve payment plan details")
          }

  def lockPaymentPlan(directDebitReference: String, paymentPlanReference: String): Action[AnyContent] =
    authorise.async:
      implicit request =>
        directDebitService
          .lockPaymentPlan(request.credentialId, paymentPlanReference)
          .map(result => Ok(Json.toJson(result)))
          .recover { case ex: Exception =>
            logger.error("Error while retrieving lock payment plan", ex)
            InternalServerError("Failed to retrieve lock payment plan")
          }

  def isDuplicatePaymentPlan(directDebitReference: String): Action[PaymentPlanDuplicateCheckRequest] =
    authorise.async(parse.json[PaymentPlanDuplicateCheckRequest]):
      implicit request =>
        directDebitService
          .isDuplicatePaymentPlan(directDebitReference, request.credentialId, request.body)
          .map(result => Ok(Json.toJson(result)))
          .recover { case ex: Exception =>
            logger.error("Error while retrieving duplicate payment plan", ex)
            InternalServerError("Failed to retrieve duplicate payment plan")
          }
