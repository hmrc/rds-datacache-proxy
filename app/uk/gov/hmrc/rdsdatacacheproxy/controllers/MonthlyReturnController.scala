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
import uk.gov.hmrc.rdsdatacacheproxy.services.MonthlyReturnService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MonthlyReturnController @Inject()(
                                         authorise: AuthAction,
                                         monthlyReturnService: MonthlyReturnService,
                                         cc: ControllerComponents
                                       )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  private val TonHeader = "X-Tax-Office-Number"
  private val TorHeader = "X-Tax-Office-Reference"

  def retrieveMonthlyReturns: Action[AnyContent] =
    authorise.async { implicit request =>
      val tonOpt = request.headers.get(TonHeader).map(_.trim).filter(_.nonEmpty)
      val torOpt = request.headers.get(TorHeader).map(_.trim).filter(_.nonEmpty)

      (tonOpt, torOpt) match {
        case (Some(ton), Some(tor)) =>
          logger.debug(s"retrieveMonthlyReturns: received $TonHeader and $TorHeader")
          monthlyReturnService
            .retrieveMonthlyReturns(ton, tor) 
            .map(res => Ok(Json.toJson(res)))
            .recover {
              case u: uk.gov.hmrc.http.UpstreamErrorResponse =>
                Status(u.statusCode)(Json.obj("message" -> u.message))
              case t: Throwable =>
                logger.error("retrieveMonthlyReturns failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
            }

        case _ =>
          val missing = Seq(
            TonHeader -> tonOpt.isDefined,
            TorHeader -> torOpt.isDefined
          ).collect { case (name, present) if !present => name }

          Future.successful(
            BadRequest(Json.obj("message" -> s"Missing or empty header(s): ${missing.mkString(", ")}"))
          )
      }
    }
}
