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
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.{CreateDirectDebitRequest, GenerateDdiRefRequest, WorkingDaysOffsetRequest}
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDPaymentPlans, EarliestPaymentDate, UserDebits}
import uk.gov.hmrc.rdsdatacacheproxy.services.DirectDebitService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitController @Inject()(
                                       authorise: AuthAction,
                                       directDebitService: DirectDebitService,
                                       cc: ControllerComponents
                                     )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging:

  def retrieveDirectDebits(firstRecordNumber: Option[Int], maxRecords: Option[Int]): Action[AnyContent] =
    authorise.async:
      implicit request =>
        (firstRecordNumber.getOrElse(1), maxRecords.getOrElse(99)) match {
          case (_, 0) =>
            Future.successful(Ok(Json.toJson(UserDebits.empty)))
          case (start, max) if start > 0 && 0 < max && max <= 99 =>
            logger.info(s"**** Cred ID: ${request.credentialId}, FirstRecordNumber: ${start}, Max Records: ${max}")
            directDebitService.retrieveDirectDebits(request.credentialId, start, max)
              .map(result => Ok(Json.toJson(result)))
          case _ =>
            Future.successful(
              BadRequest(s"Invalid firstRecordNumber: $firstRecordNumber and maxRecordNumber: $maxRecords")
            )
        }

  def createDirectDebit(): Action[CreateDirectDebitRequest] =
    authorise.async(parse.json[CreateDirectDebitRequest]):
      implicit request =>
        Future.successful(Created(request.body.paymentReference))


  def workingDaysOffset(): Action[WorkingDaysOffsetRequest] =
    Action(parse.json[WorkingDaysOffsetRequest]).async:
      implicit request =>
        directDebitService.getEarliestPaymentDate(request.body.baseDate, request.body.offsetWorkingDays)
          .map { result =>
            Ok(Json.toJson(result))
          }
          .recover {
            case ex: Exception =>
              logger.error("Error while calculating earliest payment date", ex)
              InternalServerError("Failed to calculate earliest payment date.")
          }

  def generateDDIReference(): Action[GenerateDdiRefRequest] =
    authorise.async(parse.json[GenerateDdiRefRequest]) { implicit request =>
      val body = request.body
      directDebitService.getDDIReference(
          body.paymentReference,
          request.credentialId,
          request.sessionId.value
        )
        .map { ddiReference => Ok(Json.toJson(ddiReference)) }
        .recover {
          case ex: Exception =>
            logger.error("Error while generating DDI Reference", ex)
            InternalServerError("Failed to generate DDI Reference.")
        }
    }

  def retrieveDirectDebitPaymentPlans(paymentReference: String,
                                       firstRecordNumber: Option[Int],
                                       maxRecords: Option[Int]
                                     ): Action[AnyContent] =
    authorise.async { implicit request =>
      (firstRecordNumber.getOrElse(1), maxRecords.getOrElse(99)) match {
        case (_, 0) =>
          Future.successful(Ok(Json.toJson(DDPaymentPlans.empty)))
        case (start, max) if start > 0 && 0 < max && max <= 99 =>
          logger.info(
            s"**** Cred ID: ${request.credentialId}, Payment Reference: $paymentReference, " +
              s"FirstRecordNumber: $start, Max Records: $max"
          )
          directDebitService
            .getDirectDebitPaymentPlans(paymentReference, request.credentialId, start, max)
            .map(result => Ok(Json.toJson(result)))
        case _ =>
          Future.successful(
            BadRequest(s"Invalid paymentReference: $paymentReference " +
              s"firstRecordNumber: $firstRecordNumber and maxRecordNumber: $maxRecords")
          )
      }
    }
