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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.GamblingError
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.GamblingError.*
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.GamblingService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GamblingController @Inject() (authorise: AuthAction, service: GamblingService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getMgdDetails(mgdRegNumber: String): Action[AnyContent] =
    authorise.async { implicit request =>

      service.getMgdDetails(mgdRegNumber).map {

        case Right(details) =>
          Ok(Json.toJson(details))

        case Left(error) =>
          val logMessage =
            s"[GamblingController][getMgdDetails] code=${error.code} mgdRegNumber=$mgdRegNumber"

          handleError(error, logMessage)
      }
    }

  def getReturnSummary(mgdRegNumber: String): Action[AnyContent] = authorise.async { implicit request =>

    service.getReturnSummary(mgdRegNumber).map {
      case Right(summary) => Ok(Json.toJson(summary))
      case Left(error) =>
        val logMessage = s"[GamblingController][getReturnSummary] code=${error.code} mgdRegNumber=$mgdRegNumber"
        handleError(error, logMessage)
    }
  }
  def getBusinessName(mgdRegNumber: String): Action[AnyContent] = authorise.async { implicit request =>

    service.getBusinessName(mgdRegNumber).map {
      case Right(summary) => Ok(Json.toJson(summary))
      case Left(error) =>
        val logMessage = s"[GamblingController][getBusinessName] code=${error.code} mgdRegNumber=$mgdRegNumber"
        handleError(error, logMessage)
    }
  }

  def getBusinessDetails(mgdRegNumber: String): Action[AnyContent] = authorise.async { implicit request =>

    service.getBusinessDetails(mgdRegNumber).map {
      case Right(summary) => Ok(Json.toJson(summary))
      case Left(error) =>
        val logMessage = s"[GamblingController][getBusinessDetails] code=${error.code} mgdRegNumber=$mgdRegNumber"
        handleError(error, logMessage)
    }
  }

  def getMgdCertificate(mgdRegNumber: String): Action[AnyContent] = authorise.async { implicit request =>

    service.getMgdCertificate(mgdRegNumber).map {
      case Right(certificate) =>
        Ok(Json.toJson(certificate))
      case Left(error) =>
        val logMessage = s"[GamblingController][getMgdCertificate] code=${error.code} mgdRegNumber=$mgdRegNumber"
        handleError(error, logMessage)
    }
  }

  def getOperatorDetails(mgdRegNumber: String): Action[AnyContent] =
    authorise.async { implicit request =>

      service.getOperatorDetails(mgdRegNumber).map {
        case Right(details) =>
          Ok(Json.toJson(details))

        case Left(error) =>
          val logMessage = s"[GamblingController][getOperatorDetails] code=${error.code} mgdRegNumber=$mgdRegNumber"
          handleError(error, logMessage)
      }
    }

  private def handleError(error: GamblingError, logMessage: String) =
    error match {
      case InvalidMgdRegNumber =>
        logger.warn(logMessage)
        BadRequest(errorResponse(error))
      case UnexpectedError =>
        logger.error(logMessage)
        InternalServerError(errorResponse(error))
    }

  def getBusinessContactDetails(
    mgdRegNumber: String
  ): Action[AnyContent] = authorise.async { implicit request =>

    service.getBusinessContactDetails(mgdRegNumber).map {

      case Right(details) =>
        Ok(Json.toJson(details))

      case Left(error) =>
        val logMessage =
          s"[GamblingController][getBusinessContactDetails] code=${error.code} mgdRegNumber=$mgdRegNumber"
        handleError(error, logMessage)
    }
  }

  private def errorResponse(error: GamblingError) = Json.obj("code" -> error.code, "message" -> error.message)
}
