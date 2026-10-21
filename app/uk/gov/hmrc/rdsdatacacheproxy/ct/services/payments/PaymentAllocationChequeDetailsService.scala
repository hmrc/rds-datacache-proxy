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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.payments

import jakarta.inject.Inject
import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentAllocationChequeDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PaymentAllocationChequeDetailsRepository

import scala.concurrent.Future

class PaymentAllocationChequeDetailsService @Inject (repository: PaymentAllocationChequeDetailsRepository) extends Logging {

  def getPaymentAllocationChequeDetails(taxRef: Long, chequeId: Long): Future[PaymentAllocationChequeDetails] = {
    logger.info(s"Retrieving PaymentAllocationChequeDetails from repository for taxRef: $taxRef, chequeId: $chequeId")
    repository.getPaymentAllocationChequeDetails(taxRef, chequeId)
  }

}
