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

final case class GpaGroupTaxChargesQueryParams(pStartIndex: Long, pCount: Long)

object GpaGroupTaxChargesQueryParams {

  given QueryStringBindable[GpaGroupTaxChargesQueryParams] with {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, GpaGroupTaxChargesQueryParams]] = {
      for {
        pStartIndex <- summon[QueryStringBindable[Long]].bind("pStartIndex", params)
        pCount      <- summon[QueryStringBindable[Long]].bind("pCount", params)
      } yield {
        (pStartIndex, pCount) match {
          case (Right(pStartIndex), Right(pCount)) => Right(GpaGroupTaxChargesQueryParams(pStartIndex = pStartIndex, pCount = pCount))
          case (_, _) =>
            Left("Unable to bind QueryStringBindable: missing params")
        }
      }
    }

    override def unbind(key: String, value: GpaGroupTaxChargesQueryParams): String = {
      "pStartIndex" + value.pStartIndex + "&pCount" + value.pCount
    }
  }
}
