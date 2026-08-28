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

package uk.gov.hmrc.rdsdatacacheproxy.actions

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

enum IdentifiedUserRequest[B](request: Request[B]) extends WrappedRequest[B](request) {

  case OrganisationRequest(
    request: Request[B],
    taxOfficeNumber: String,
    taxOfficeReference: String
  ) extends IdentifiedUserRequest(request)

  case AgentRequest(
    request: Request[B],
    agentRef: String,
    credId: String
  ) extends IdentifiedUserRequest(request)
}

object IdentifiedUserRequest {
  private type CredId = String
  private type UserInfo = Option[AffinityGroup] ~ Enrolments ~ Option[CredId]

  def fromRequestAndUserInfo[B](request: Request[B], userInfo: UserInfo): Either[String, IdentifiedUserRequest[B]] =
    userInfo match
      case Some(Organisation) ~ enrolments ~ _ =>
        for
          cis <- enrolments.getEnrolment("HMRC-CIS-ORG") toRight "CIS enrolment not found"
          ton <- cis.getIdentifier("TaxOfficeNumber") toRight "Tax office number not found"
          tor <- cis.getIdentifier("TaxOfficeReference") toRight "Tax office reference not found"
        yield OrganisationRequest(request, ton.value, tor.value)

      case Some(Agent) ~ enrolments ~ credIdOpt =>
        for
          irPaye   <- enrolments.getEnrolment("IR-PAYE-AGENT") toRight "IR PAYE enrolment not found"
          agentRef <- irPaye.getIdentifier("IRAgentReference") toRight "Agent reference not found"
          credId   <- credIdOpt toRight "Credential ID not found"
        yield AgentRequest(request, agentRef.value, credId)

      case _ => Left("User must be Organisation or Agent")
}
