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

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.mvc.Results.Forbidden
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.AgentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentAuthAction @Inject() (service: AgentService)(implicit ec: ExecutionContext) extends Logging {

  def apply(regime: String, regNumber: String): ActionFilter[AuthenticatedRequest] =
    new ActionFilter[AuthenticatedRequest] {

      override protected def executionContext: ExecutionContext = ec

      override protected def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] =
        request.affinityGroup match {
          case Some(Agent) =>
            service.hasClient(regime, request.credentialId, regNumber).map {
              case Right(true) => None
              case _ =>
                logger.warn(
                  s"[AgentAuthAction] Agent not authorised for regNumber $regNumber under regime $regime"
                )
                Some(Forbidden(Json.obj("message" -> "Agent not authorised for the requested client")))
            }
          case _ => Future(None)
        }
    }
}
