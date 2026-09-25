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

final case class GpaPaymentDetailsQueryParams(contractVersion: Int, startIndex: Int, count: Int)

object GpaPaymentDetailsQueryParams {

  given QueryStringBindable[GpaPaymentDetailsQueryParams] with {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, GpaPaymentDetailsQueryParams]] = {
      for {
        contractVersionE <- summon[QueryStringBindable[Int]].bind("contractVersion", params)
        startIndexE      <- summon[QueryStringBindable[Int]].bind("startIndex", params)
        countE           <- summon[QueryStringBindable[Int]].bind("count", params)
      } yield {
        (contractVersionE, startIndexE, countE) match
          case (Right(contractVersion), Right(startIndex), Right(count)) =>
            Right(GpaPaymentDetailsQueryParams(contractVersion, startIndex, count))
          case (_, _, _) =>
            Left("Unable to bind GpaPaymentDetailsQueryParams: missing params")
      }
    }

    override def unbind(key: String, value: GpaPaymentDetailsQueryParams): String = {
      "contractVersion=" + value.contractVersion.toString + "&" +
        "startIndex=" + value.startIndex.toString + "&" +
        "count=" + value.count.toString
    }

  }

}
