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

// TEMPORARY — local testing only. Bypasses auth entirely; do not merge or deploy this file.
// Revert: delete this file and switch the AuthAction binding in Module.scala back to DefaultAuthAction.

import play.api.mvc.*
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.SessionId

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NoAuthAction @Inject() (val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends AuthAction {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(AuthenticatedRequest(request, "local-internal-id", "local-cred-id", SessionId("local-session"), Enrolments(Set.empty)))
}
