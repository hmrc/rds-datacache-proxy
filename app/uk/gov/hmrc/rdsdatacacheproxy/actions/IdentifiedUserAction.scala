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

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{ActionRefiner, Request, Result, Results}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IdentifiedUserAction @Inject() (val authConnector: AuthConnector)(using protected val executionContext: ExecutionContext)
    extends ActionRefiner[Request, IdentifiedUserRequest]
    with Results
    with AuthorisedFunctions {

  protected def refine[A](request: Request[A]): Future[Either[Result, IdentifiedUserRequest[A]]] =
    given HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    authorised()
      .retrieve(Retrievals.affinityGroup and Retrievals.allEnrolments and Retrievals.internalId) { userInfo =>
        Future.successful(
          IdentifiedUserRequest
            .fromRequestAndUserInfo(request, userInfo)
            .left
            .map { msg =>
              Forbidden(Json.toJson(ErrorResponse(statusCode = 403, msg)))
            }
        )
      }
}
