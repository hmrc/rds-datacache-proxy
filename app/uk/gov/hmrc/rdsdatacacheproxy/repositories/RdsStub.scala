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

package uk.gov.hmrc.rdsdatacacheproxy.repositories

import uk.gov.hmrc.rdsdatacacheproxy.models.responses.{DDIReference, DDPaymentPlans, DirectDebit, EarliestPaymentDate, PaymentPlan, UserDebits}
import uk.gov.hmrc.rdsdatacacheproxy.utils.StubUtils

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RdsStub @Inject()() extends RdsDataSource:
  //  Once it's a connector, inject:
  //  httpClientV2: HttpClientV2
  //  servicesConfig: ServicesConfig

  //  and define:
  //  val serviceUrl: String = servicesConfig.baseUrl("rds")

  // Remove this once real stubbing exists
  private[repositories] val stubData = new StubUtils()

  def getDirectDebits(id: String, start: Int, max: Int): Future[UserDebits] =
    val debits: Seq[DirectDebit] = for(i <- 1 to max) yield stubData.randomDirectDebit(i)
    Future.successful(UserDebits(debits.size, debits))

  def getEarliestPaymentDate(baseDate: LocalDate, offsetWorkingDays: Int): Future[EarliestPaymentDate] =
    Future.successful(EarliestPaymentDate(baseDate.plusDays(offsetWorkingDays)))

  def getDirectDebitReference(paymentReference: String, credId: String, sessionId: String): Future[DDIReference] =
    Future.successful(DDIReference(paymentReference))

  def getDirectDebitPaymentPlans(paymentReference: String, credId: String, start: Int, max: Int):
    Future[DDPaymentPlans] = {
    val plans: Seq[PaymentPlan] = for (i <- 1 to max) yield stubData.randomPaymentPlan(i)
    Future.successful(DDPaymentPlans("sort code", "account Number", "account name", plans.size, plans))
  }

