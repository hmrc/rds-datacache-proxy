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

package uk.gov.hmrc.rdsdatacacheproxy.actions

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.auth.core.Enrolments
import AuthenticatedRequest.*
import scala.concurrent.Future
import play.api.mvc.Results
import play.api.libs.json.Json
import play.api.mvc.Result

case class AuthenticatedRequest[A](
  private val request: Request[A],
  internalId: String,
  credentialId: String,
  sessionId: SessionId,
  enrolments: Enrolments
) extends WrappedRequest[A](request) {

  def whenUserAuthorisedForCharity(charityReference: String)(block: => Future[Result]): Future[Result] =
    if (enrolments.getEnrolment(agentEnrolmentKey).isDefined)
      block
    else
      enrolments.getEnrolment(organisationEnrolmentKey) match {
        case None =>
          Future.successful(Results.Unauthorized)
        case Some(enrolment) =>
          val enrolledCharityReference =
            enrolment.getIdentifier(organisationIdentifierKey).map(_.value.trim).filter(_.nonEmpty)

          if (enrolledCharityReference.contains(charityReference))
            block
          else
            Future.successful(
              Results.Forbidden(Json.obj("message" -> "Not authorised for the requested charity reference"))
            )
      }

  def whenAgentAuthorisedForCharity(agentReference: String)(block: => Future[Result]): Future[Result] =
    enrolments.getEnrolment(agentEnrolmentKey) match {
      case None =>
        Future.successful(Results.Unauthorized)

      case Some(enrolment) =>
        val enrolledAgentReference =
          enrolment.getIdentifier(agentIdentifierKey).map(_.value.trim).filter(_.nonEmpty)

        if (enrolledAgentReference.contains(agentReference))
          block
        else
          Future.successful(
            Results.Forbidden(Json.obj("message" -> "Agent not authorised for the requested agent reference"))
          )
    }

}

object AuthenticatedRequest {
  val organisationEnrolmentKey = "HMRC-CHAR-ORG"
  val organisationIdentifierKey = "CHARID"
  val agentEnrolmentKey = "HMRC-CHAR-AGENT"
  val agentIdentifierKey = "AGENTCHARID"
}
