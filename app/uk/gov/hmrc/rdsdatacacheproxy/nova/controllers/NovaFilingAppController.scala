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

package uk.gov.hmrc.rdsdatacacheproxy.nova.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.nova.repositories.NovaDataSource

import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NovaFilingAppController @Inject() (
  authorise: AuthAction,
  novaDataSource: NovaDataSource,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getTraderInformation(vrn: String, gracePeriod: Option[Int]): Action[AnyContent] = authorise.async { implicit request =>
    if (vrn.trim.isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "vrn must be provided")))
    } else {
      novaDataSource
        .getTraderInformation(vrn, gracePeriod)
        .map {
          case Some(info) => Ok(Json.toJson(info))
          case None =>
            NotFound(Json.obj("code" -> "TRADER_NOT_FOUND", "message" -> s"No trader found for VRN $vrn"))
        }
        .recover { case ex: Exception =>
          logger.error(s"[NOVA] Error retrieving trader information for vrn=$vrn", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve trader information"))
        }
    }
  }

  def getVehicleStatus(vin: String): Action[AnyContent] = authorise.async { implicit request =>
    if (vin.trim.isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "vin must be provided")))
    } else {
      novaDataSource
        .getVehicleStatusDetails(vin)
        .map {
          case Some(details) => Ok(Json.toJson(details))
          case None =>
            NotFound(Json.obj("code" -> "VEHICLE_NOT_FOUND", "message" -> s"No vehicle found for VIN $vin"))
        }
        .recover { case ex: Exception =>
          logger.error(s"[NOVA] Error retrieving vehicle status for vin=$vin", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve vehicle status"))
        }
    }
  }

  def getVehicleCalculationData(
    fromCurrency: String,
    invoiceDate: String,
    arrivalDate: String
  ): Action[AnyContent] = authorise.async { implicit request =>
    val parsedDates = for {
      invoice <- parseDate(invoiceDate, "invoiceDate")
      arrival <- parseDate(arrivalDate, "arrivalDate")
    } yield (invoice, arrival)

    parsedDates match {
      case Left(errorMsg) =>
        Future.successful(BadRequest(Json.obj("error" -> errorMsg)))
      case Right((invoice, arrival)) =>
        novaDataSource
          .getVehicleCalculationData(fromCurrency, invoice, arrival)
          .map(data => Ok(Json.toJson(data)))
          .recover { case ex: Exception =>
            logger.error(
              s"[NOVA] Error retrieving vehicle calculation data for fromCurrency=$fromCurrency",
              ex
            )
            InternalServerError(Json.obj("error" -> "Failed to retrieve vehicle calculation data"))
          }
    }
  }

  def getEuMemberStates(): Action[AnyContent] = authorise.async { implicit request =>
    novaDataSource
      .getEuMemberStates()
      .map(response => Ok(Json.toJson(response)))
      .recover { case ex: Exception =>
        logger.error("[NOVA] Error retrieving EU member states", ex)
        InternalServerError(Json.obj("error" -> "Failed to retrieve EU member states"))
      }
  }

  def getNvraKnownFacts(nvraRefNumber: String): Action[AnyContent] = authorise.async { implicit request =>
    if (nvraRefNumber.trim.isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "nvraRefNumber must be provided")))
    } else {
      novaDataSource
        .getNvraKnownFacts(nvraRefNumber)
        .map(facts => Ok(Json.toJson(facts)))
        .recover { case ex: Exception =>
          logger.error(s"[NOVA] Error retrieving NVRA known facts for nvraRefNumber=$nvraRefNumber", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve NVRA known facts"))
        }
    }
  }

  private def parseDate(value: String, paramName: String): Either[String, LocalDate] =
    try Right(LocalDate.parse(value))
    catch { case _: DateTimeParseException => Left(s"$paramName must be a valid ISO-8601 date (yyyy-MM-dd)") }
}
