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

package uk.gov.hmrc.rdsdatacacheproxy.connectors

import uk.gov.hmrc.rdsdatacacheproxy.models.DirectDebit

import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RDSConnector @Inject()() {
  //  Once it's a connector, inject:
  //  httpClientV2: HttpClientV2
  //  servicesConfig: ServicesConfig

  //  and define:
  //  val serviceUrl: String = servicesConfig.baseUrl("rds")

  private[connectors] def defaultDD(i: Int): DirectDebit =
    DirectDebit.apply(
      ddiRefNumber = s"defaultRef$i",
      LocalDateTime.parse("2020-02-02T22:22:22"),
      "00-00-00",
      "00000000",
      "BankLtd",
      false,
      i
    )

  def getDirectDebits(id: String, offset: Option[LocalDate] = None, limit: Option[Int] = None): Future[Seq[DirectDebit]] =
    limit match {
      case None => Future.successful(Seq(defaultDD(1)))
      case Some(l) =>
        val debits = for(i <- 1 to l) yield defaultDD(i)
        Future.successful(debits)
    }
}