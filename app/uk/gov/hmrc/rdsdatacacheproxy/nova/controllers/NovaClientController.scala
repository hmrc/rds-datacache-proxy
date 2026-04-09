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

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NovaClientController @Inject() (
  authorise: AuthAction,
  novaDataSource: NovaDataSource,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int): Action[AnyContent] = authorise.async {
    implicit request =>
      novaDataSource
        .getClientListDownloadStatus(credentialId, serviceName, gracePeriod)
        .map(status => Ok(Json.obj("status" -> status)))
        .recover { case ex: Exception =>
          logger.error(s"[NOVA] Error retrieving client list download status for credentialId=$credentialId", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve client list download status"))
        }
  }

  def getAllClients(credentialId: String, start: Int, count: Int, sort: Int, ascending: Boolean): Action[AnyContent] = authorise.async {
    implicit request =>
      val order = if (ascending) "ASC" else "DESC"
      novaDataSource
        .getAllClients(credentialId, start, count, sort, order)
        .map(result => Ok(Json.toJson(result)))
        .recover { case ex: Exception =>
          logger.error(s"[NOVA] Error retrieving client list for credentialId=$credentialId", ex)
          InternalServerError(Json.obj("error" -> "Failed to retrieve client list"))
        }
  }

  def searchClients(
    credentialId: String,
    vrn: Option[String],
    name: Option[String],
    nameStart: Option[String],
    start: Int,
    count: Int,
    sort: Int,
    ascending: Boolean
  ): Action[AnyContent] = authorise.async { implicit request =>
    val order = if (ascending) "ASC" else "DESC"
    val searchFuture = (vrn.map(_.trim).filter(_.nonEmpty), name.map(_.trim).filter(_.nonEmpty), nameStart.map(_.trim).filter(_.nonEmpty)) match {
      case (Some(v), _, _)  => novaDataSource.getClientsByVrn(credentialId, v)
      case (_, Some(n), _)  => novaDataSource.getClientsByName(credentialId, n, start, count, sort, order)
      case (_, _, Some(ns)) => novaDataSource.getClientsByNameStart(credentialId, ns, start, count, sort, order)
      case _                => novaDataSource.getAllClients(credentialId, start, count, sort, order)
    }
    searchFuture
      .map(result => Ok(Json.toJson(result)))
      .recover { case ex: Exception =>
        logger.error(s"[NOVA] Error searching clients for credentialId=$credentialId", ex)
        InternalServerError(Json.obj("error" -> "Failed to search clients"))
      }
  }

  def hasClient(credentialId: String, vrn: String): Action[AnyContent] = authorise.async { implicit request =>
    if (vrn.trim.isEmpty) {
      Future.successful(BadRequest(Json.obj("error" -> "vrn must be provided")))
    } else {
      novaDataSource
        .hasClient(credentialId, vrn)
        .map(exists => Ok(Json.obj("exists" -> exists)))
        .recover { case ex: Exception =>
          logger.error(s"[NOVA] Error checking hasClient for credentialId=$credentialId, vrn=$vrn", ex)
          InternalServerError(Json.obj("error" -> "Failed to check client existence"))
        }
    }
  }
}
