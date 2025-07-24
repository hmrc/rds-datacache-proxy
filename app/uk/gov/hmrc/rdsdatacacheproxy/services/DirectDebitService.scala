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

package uk.gov.hmrc.rdsdatacacheproxy.services

import uk.gov.hmrc.rdsdatacacheproxy.models.{DirectDebit, DirectDebits}

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter.ISO_DATE
import javax.inject.Inject
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class DirectDebitService @Inject()():

  val defaultDD: DirectDebit =
    DirectDebit.apply(
      ddiRefNumber = "defaultRef",
      LocalDateTime.now(),
      "00-00-00",
      "00000000",
      "Bank Ltd",
      false,
      0)

  def retrieveDirectDebits(id: String): Future[DirectDebits] =
    Future.successful(DirectDebits(Seq(defaultDD)))

  def retrieveDirectDebitsWithOffset(id: String, offset: String, limit: Int): Future[DirectDebits] =
    parseStringToDate(offset) match
      case Some(offsetDate) => Future.successful(DirectDebits(Seq(defaultDD)))
      case None => Future.failed(new Exception("Invalid date provided for offset"))

  private[services] def parseStringToDate(date: String): Option[LocalDate] =
    Try (
      LocalDate.parse(date, ISO_DATE)
    ) match
      case Success(validDate) => Some(validDate)
      case Failure(_) => None
