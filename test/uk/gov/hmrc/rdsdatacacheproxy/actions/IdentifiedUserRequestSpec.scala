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

import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.rdsdatacacheproxy.actions.IdentifiedUserRequest.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase

class IdentifiedUserRequestSpec extends SpecBase {
  import play.api.test.FakeRequest

  "IdentifiedUserRequest factory must" - {

    "fail when" - {
      "user is an individual" in {
        val anyIndividualInfo = Some(Individual) ~ Enrolments(Set.empty) ~ Some("")

        val result = IdentifiedUserRequest.fromRequestAndUserInfo(FakeRequest(), anyIndividualInfo)

        result.left.value mustBe "User must be Organisation or Agent"
      }
      "user is an Organisation not enrolled in CIS" in {
        val badOrganisationInfo = Some(Organisation) ~ Enrolments(Set.empty) ~ Some("")

        val result = IdentifiedUserRequest.fromRequestAndUserInfo(FakeRequest(), badOrganisationInfo)

        result.left.value mustBe "CIS enrolment not found"
      }
      "user is an Agent not enrolled in IR PAYE" in {
        val badAgentInfo = Some(Agent) ~ Enrolments(Set.empty) ~ Some("")

        val result = IdentifiedUserRequest.fromRequestAndUserInfo(FakeRequest(), badAgentInfo)

        result.left.value mustBe "IR PAYE enrolment not found"
      }
    }
    "succeed when" - {
      "user is an Organisation enrolled in CIS" in {
        val givenTon = "123"
        val givenTor = "AB12345"
        val cisEnrolments = makeEnrolments(
          "HMRC-CIS-ORG" -> Map(
            "TaxOfficeNumber"    -> givenTon,
            "TaxOfficeReference" -> givenTor
          )
        )
        val goodOrganisationInfo = Some(Organisation) ~ cisEnrolments ~ Some("")

        val result = IdentifiedUserRequest.fromRequestAndUserInfo(FakeRequest(), goodOrganisationInfo)

        result.value match
          case OrganisationRequest(_, actualTon, actualTor) =>
            actualTon mustBe givenTon
            actualTor mustBe givenTor

          case other => fail(s"Expected OrganisationRequest; instead received: $other")
      }
      "user is an Agent enrolled in IR PAYE" in {
        val givenAgentRef = "1234"
        val givenCredId = "5678"
        val irPayeEnrolments = makeEnrolments(
          "IR-PAYE-AGENT" -> Map("IRAgentReference" -> givenAgentRef)
        )
        val goodAgentInfo = Some(Agent) ~ irPayeEnrolments ~ Some(givenCredId)

        val result = IdentifiedUserRequest.fromRequestAndUserInfo(FakeRequest(), goodAgentInfo)

        result.value match
          case AgentRequest(_, actualAgentRef, actualCredId) =>
            actualAgentRef mustBe givenAgentRef
            actualCredId mustBe givenCredId

          case other => fail(s"Expected AgentRequest; instead received: $other")
      }
    }
  }
}
