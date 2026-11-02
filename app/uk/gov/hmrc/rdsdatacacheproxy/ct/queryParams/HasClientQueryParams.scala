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

package uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams

import play.api.mvc.QueryStringBindable

final case class HasClientQueryParams(
  utr: Long,
  irAgentId: String,
  credentialId: String
)

object HasClientQueryParams {

  given QueryStringBindable[HasClientQueryParams] with {

    override def bind(
      key: String,
      params: Map[String, Seq[String]]
    ): Option[Either[String, HasClientQueryParams]] = {

      val longBinder = summon[QueryStringBindable[Long]]
      val stringBinder = summon[QueryStringBindable[String]]

      val utrE = longBinder.bind("utr", params)
      val irAgentIdE = stringBinder.bind("irAgentId", params)
      val credentialIdE = stringBinder.bind("credentialId", params)

      Some(
        for {
          utr          <- utrE.getOrElse(Left("Missing utr"))
          irAgentId    <- irAgentIdE.getOrElse(Left("Missing irAgentId"))
          credentialId <- credentialIdE.getOrElse(Left("Missing credentialId"))
        } yield HasClientQueryParams(
          utr          = utr,
          irAgentId    = irAgentId,
          credentialId = credentialId
        )
      )
    }

    override def unbind(key: String, value: HasClientQueryParams): String =
      Seq(
        "utr=" + value.utr.toString,
        "irAgentId=" + value.irAgentId,
        "credentialId=" + value.credentialId
      ).mkString("&")
  }
}
