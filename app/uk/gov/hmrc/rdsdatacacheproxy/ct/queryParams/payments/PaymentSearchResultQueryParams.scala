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

package uk.gov.hmrc.rdsdatacacheproxy.ct.queryParams.payments

import play.api.mvc.QueryStringBindable

import java.time.LocalDate
import scala.util.Try

final case class PaymentSearchResultQueryParams(fromDate: Option[LocalDate], toDate: Option[LocalDate])

object PaymentSearchResultQueryParams {

  given QueryStringBindable[PaymentSearchResultQueryParams] with {

    override def bind(
      key: String,
      params: Map[String, Seq[String]]
    ): Option[Either[String, PaymentSearchResultQueryParams]] = {

      val stringBinder = summon[QueryStringBindable[String]]

      val fromDateE = stringBinder.bind("fromDate", params)
      val toDateE = stringBinder.bind("toDate", params)

      def parseDate(
        value: Option[Either[String, String]]
      ): Either[String, Option[LocalDate]] =
        value match {
          case None =>
            Right(None)

          case Some(Left(error)) =>
            Left(error)

          case Some(Right(date)) if date.trim.isEmpty =>
            Right(None)

          case Some(Right(date)) =>
            Try(LocalDate.parse(date)).toEither.left
              .map(_ => s"Invalid date: $date")
              .map(Some(_))
        }

      Some(
        for {
          fromDate <- parseDate(fromDateE)
          toDate   <- parseDate(toDateE)
        } yield {
          PaymentSearchResultQueryParams(
            fromDate = fromDate,
            toDate   = toDate
          )
        }
      )
    }

    override def unbind(key: String, value: PaymentSearchResultQueryParams): String = {
      val params = Seq(
        value.fromDate.map(d => "fromDate=" + d.toString),
        value.toDate.map(d => "toDate=" + d.toString)
      ).flatten

      params.mkString("&")
    }
  }
}
