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

package uk.gov.hmrc.rdsdatacacheproxy.cis.controllers

import play.api.Logging
import play.api.libs.json.{JsError, JsPath, JsValue, Json, JsonValidationError}
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.*
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.cis.services.PrepopService

import javax.inject.Inject
import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class PrepopController @Inject() (
  authorise: AuthAction,
  service: PrepopService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  private def badJson(
    errs: Seq[(JsPath, Seq[JsonValidationError])]
  ): Result =
    BadRequest(
      Json.obj(
        "message" -> "Invalid JSON body",
        "errors"  -> JsError.toJson(errs)
      )
    )

  private def context(kf: PrepopKnownFacts): String =
    s"TON=${kf.taxOfficeNumber}, TOR=${kf.taxOfficeReference}, AO=${kf.agentOwnReference}"

  def getSchemePrepopByKnownFacts: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[PrepopKnownFacts]
        .fold(
          errs => Future.successful(badJson(errs)),
          knownFacts => {
            val ctx = context(knownFacts)

            service
              .getSchemePrepopByKnownFacts(
                knownFacts.taxOfficeNumber,
                knownFacts.taxOfficeReference,
                knownFacts.agentOwnReference
              )
              .map { scheme =>
                val body = PrePopContractorBody(
                  schemeName = scheme.schemeName,
                  utr        = scheme.utr.getOrElse(""),
                  response   = 0
                )

                Ok(Json.toJson(PrePopContractorResponse(knownfacts = knownFacts, prePopContractor = body)))
              }
              .recover {
                case _: NoSuchElementException =>
                  NotFound(Json.obj("message" -> s"No CIS scheme pre-pop data found for $ctx"))

                case NonFatal(t) =>
                  logger.error(s"[getSchemePrepopByKnownFacts] failed for $ctx", t)
                  InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
          }
        )
    }

  def getSubcontractorsPrepopByKnownFacts: Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[PrepopKnownFacts]
        .fold(
          errs => Future.successful(badJson(errs)),
          knownFacts => {
            val ctx = context(knownFacts)

            service
              .getSubcontractorsPrepopByKnownFacts(
                knownFacts.taxOfficeNumber,
                knownFacts.taxOfficeReference,
                knownFacts.agentOwnReference
              )
              .map { subcontractors =>
                val subs = subcontractors.map { r =>
                  PrePopSubcontractor(
                    subcontractorType  = r.subcontractorType,
                    utr                = r.subcontractorUtr,
                    verificationNumber = r.verificationNumber,
                    verificationSuffix = r.verificationSuffix.getOrElse(""),
                    title              = r.title.getOrElse(""),
                    firstName          = r.firstName.getOrElse(""),
                    secondName         = r.secondName.getOrElse(""),
                    surname            = r.surname.getOrElse("")
                  )
                }

                val body = PrePopSubcontractorsBody(
                  response       = 0,
                  subcontractors = subs
                )

                Ok(
                  Json.toJson(
                    PrePopSubcontractorsResponse(
                      knownfacts           = knownFacts,
                      prePopSubcontractors = body
                    )
                  )
                )
              }
              .recover {
                case _: NoSuchElementException =>
                  NotFound(Json.obj("message" -> s"No CIS subcontractor pre-pop data found for $ctx"))

                case NonFatal(t) =>
                  logger.error(s"[getSubcontractorsPrepopByKnownFacts] failed for $ctx", t)
                  InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
          }
        )
    }
}
