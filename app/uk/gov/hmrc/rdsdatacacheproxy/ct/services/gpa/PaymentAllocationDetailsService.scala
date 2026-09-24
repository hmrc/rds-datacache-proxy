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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PaymentAllocationDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.PaymentAllocationDetailsRepository

import javax.inject.Inject
import scala.concurrent.Future

class PaymentAllocationDetailsService @Inject() (paymentAllocationDetailsRepository: PaymentAllocationDetailsRepository) extends Logging {

  def getGPAPaymentAllocationDetail(gpaUtr: Long,
                                    gppContractVersion: Long,
                                    participatorUtr: Long,
                                    participatorAp: Long,
                                    startIndex: Long,
                                    count: Long
                                   ): Future[PaymentAllocationDetails] = {
    logger.info(
      s"Calling repository for gpaUtr: $gpaUtr, gppContractVersion: $gppContractVersion, participatorUtr: $participatorUtr, participatorAp: $participatorAp, startIndex: $startIndex and count: $count"
    )

    paymentAllocationDetailsRepository.getGPAPaymentAllocationDetail(gpaUtr: Long,
                                                                     gppContractVersion: Long,
                                                                     participatorUtr: Long,
                                                                     participatorAp: Long,
                                                                     startIndex: Long,
                                                                     count: Long
                                                                    )
  }
}
