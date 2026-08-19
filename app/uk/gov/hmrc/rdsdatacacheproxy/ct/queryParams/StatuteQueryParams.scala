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

import java.net.URLEncoder
import java.time.LocalDate
import scala.util.Try

final case class StatuteQueryParams(ruleKey: String, startDate: LocalDate, endDate: LocalDate)

object StatuteQueryParams {

  given QueryStringBindable[StatuteQueryParams] with {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, StatuteQueryParams]] = {
      for {
        ruleKeyE   <- summon[QueryStringBindable[String]].bind("ruleKey", params)
        startDateE <- summon[QueryStringBindable[String]].bind("startDate", params)
        endDateE   <- summon[QueryStringBindable[String]].bind("endDate", params)
      } yield {
        (ruleKeyE, startDateE, endDateE) match
          case (Right(ruleKey), Right(startDateE), Right(endDateE)) =>
            val startDateLocalDateE = Try(LocalDate.parse(startDateE)).toEither
            val endDateLocalDateE = Try(LocalDate.parse(endDateE)).toEither
            (startDateLocalDateE, endDateLocalDateE) match {
              case (Right(startDateLocalDate), Right(endDateLocalDate)) =>
                Right(
                  StatuteQueryParams(ruleKey = ruleKey, startDate = startDateLocalDate, endDate = endDateLocalDate)
                )
              case (_, _) =>
                Left("Unable to bind QueryStringBindable: date conversion")
            }

          case (_, _, _) =>
            Left("Unable to bind QueryStringBindable: missing params")
      }
    }

    override def unbind(key: String, value: StatuteQueryParams): String = {
      "ruleKey=" + URLEncoder.encode(value.ruleKey, "utf-8") + "&" +
        "startDate=" + value.startDate.toString + "&" +
        "endDate=" + value.endDate.toString
    }

  }

}
