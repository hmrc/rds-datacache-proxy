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

package uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.gpa

import play.api.mvc.QueryStringBindable

final case class PaymentAllocationDetailsQueryParams(startIndex: Long, count: Long)

object PaymentAllocationDetailsQueryParams {

  given QueryStringBindable[PaymentAllocationDetailsQueryParams] with {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, PaymentAllocationDetailsQueryParams]] = {
      for {
        startIndex <- summon[QueryStringBindable[Long]].bind("startIndex", params)
        count      <- summon[QueryStringBindable[Long]].bind("count", params)
      } yield {
        (startIndex, count) match {
          case (Right(startIndex), Right(count)) => Right(PaymentAllocationDetailsQueryParams(startIndex = startIndex, count = count))
          case (_, _) =>
            Left("Unable to bind QueryStringBindable: missing params")
        }
      }
    }

    override def unbind(key: String, value: PaymentAllocationDetailsQueryParams): String = {
      "startIndex: " + value.startIndex + "& count: " + value.count
    }
  }
}
