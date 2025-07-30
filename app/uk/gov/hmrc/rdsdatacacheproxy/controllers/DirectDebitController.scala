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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.models.UserDebits
import uk.gov.hmrc.rdsdatacacheproxy.models.UserDebits.*
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.CreateDirectDebitRequest
import uk.gov.hmrc.rdsdatacacheproxy.models.requests.{CreateDirectDebitRequest, WorkingDaysOffsetRequest}
import uk.gov.hmrc.rdsdatacacheproxy.models.DirectDebit.*
import uk.gov.hmrc.rdsdatacacheproxy.models.responses.EarliestPaymentDate
import uk.gov.hmrc.rdsdatacacheproxy.services.DirectDebitService

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DirectDebitController @Inject()(
  authorise: AuthAction,
  directDebitService: DirectDebitService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext) extends BackendController(cc):

  def retrieveDirectDebits(firstRecordNumber: Option[Int], maxRecords: Option[Int]): Action[AnyContent] =
    authorise.async:
      implicit request =>
        (firstRecordNumber.getOrElse(1), maxRecords.getOrElse(99)) match {
          case (_, 0) =>
            Future.successful(Ok(Json.toJson(UserDebits.empty)))
          case (start, max) if start > 0 && 0 < max && max <= 99  =>
            directDebitService
              .retrieveDirectDebits(
                request.internalId,
                start,
                max
              ).map(s => Ok(Json.toJson(s)))
          case _ =>
            Future.successful(
              BadRequest(s"Invalid firstRecordNumber: $firstRecordNumber and maxRecordNumber: $maxRecords")
            )
        }

  def createDirectDebit(): Action[CreateDirectDebitRequest] =
    authorise.async(parse.json[CreateDirectDebitRequest]):
      implicit request =>
        Future.successful(Created(request.body.paymentReference))


  def getWorkingDaysOffset(): Action[WorkingDaysOffsetRequest] =
    authorise.async(parse.json[WorkingDaysOffsetRequest]):
      implicit request =>
        val maybeCurrentDate: Option[LocalDate] = Try(LocalDate.parse(request.body.baseDate)).toOption
        val maybeNumberOfWorkingDays: Option[Int] = Try(request.body.offsetWorkingDays.toInt).toOption

        (maybeCurrentDate, maybeNumberOfWorkingDays) match {
          case (Some(currentDate), None) => Future.successful(BadRequest("Could not convert offsetWorkingDays to an Int"))
          case (None, Some(numberOfWorkingDays)) => Future.successful(BadRequest("Could not convert baseDate to LocalDate"))
          case (None, None) => Future.successful(BadRequest("Cannot convert offsetWorkingDays to Int, and baseDate to LocalDate"))
          case (Some(currentDate), Some(numberOfWorkingDays)) => Future.successful(Ok(Json.toJson(EarliestPaymentDate(date = currentDate.plusDays(numberOfWorkingDays).toString))))
        }