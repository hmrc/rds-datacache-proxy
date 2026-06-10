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

package uk.gov.hmrc.rdsdatacacheproxy.euvat.actions

import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.Unauthorized
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, SessionId, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.rdsdatacacheproxy.euvat.models.requests.AuthenticatedRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultEuVatAuthAction @Inject() (
  override val authConnector: AuthConnector,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends EuVatAuthAction
    with AuthorisedFunctions
    with Logging:

  private def usingSupportedAffinityAndEnrolments(
    affinityGroup: AffinityGroup,
    enrolments: Enrolments
  ): (Boolean, String, String) = {

    // enrolment → identifier name
    val requiredIdentifiers: Map[String, String] = Map(
      "HMRC-EU-REF-ORG" -> "VATRegNo",
      "HMCE-VAT-AGNT"   -> "AgentRefNo",
      "HMRC-NOVRN-AGNT" -> "VATAgentRefNo"
    )

    // allowed enrolments per affinity group
    val allowedKeys: Set[String] = affinityGroup match {
      case AffinityGroup.Organisation | AffinityGroup.Individual => Set("HMRC-EU-REF-ORG")
      case AffinityGroup.Agent                                   => Set("HMCE-VAT-AGNT", "HMRC-NOVRN-AGNT")
      case _                                                     => Set.empty[String]
    }

    // find matching enrolment + identifier
    val identifiers: Option[(String, String)] =
      enrolments.enrolments.collectFirst {
        case enrol if enrol.isActivated && allowedKeys.contains(enrol.key) =>
          requiredIdentifiers.get(enrol.key).flatMap { requiredIdName =>
            enrol.identifiers
              .find(id => id.key == requiredIdName && id.value.trim.nonEmpty)
              .map(id => (requiredIdName, id.value))
          }
      }.flatten

    identifiers match {
      case Some((idName, idValue)) => (true, idName, idValue)

      case None => throw new UnauthorizedException("Missing or empty enrolment identifier")
    }
  }

  override def invokeBlock[A](
    request: Request[A],
    block: AuthenticatedRequest[A] => Future[Result]
  ): Future[Result] =
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    val sessionId: SessionId = hc.sessionId
      .getOrElse(throw new UnauthorizedException("Unable to retrieve session ID from headers"))

    authorised()
      .retrieve(Retrievals.affinityGroup and Retrievals.credentials and Retrievals.allEnrolments) {
        case Some(affinityGroup) ~ Some(credentials) ~ enrolments =>
          val (isValid, idKey, idValue) = usingSupportedAffinityAndEnrolments(affinityGroup, enrolments)

          block(AuthenticatedRequest(request, credentials.providerId, sessionId, idKey, idValue))
        case _ =>
          throw new UnauthorizedException("Unable to retrieve required auth values")
      }
      .recover { case _: AuthorisationException =>
        val error = "Failed to authorise request"
        logger.warn(error)
        Unauthorized(error)
      }

trait EuVatAuthAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]
