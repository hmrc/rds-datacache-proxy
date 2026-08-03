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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestAccrual
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.InterestAccrualListDatacacheRepository

import javax.inject.Inject
import scala.concurrent.Future

class InterestAccrualService @Inject() (interestAccrualListDatacacheRepository: InterestAccrualListDatacacheRepository) extends Logging {

  def getInterestAccrualList(taxRef: Long, accPeriod: Long, interestType: String): Future[List[InterestAccrual]] = {
    logger.info(
      s"[InterestAccrualService][getInterestAccrualList] Calling repository for taxRef: $taxRef, accPerioud: $accPeriod, interestType: $interestType"
    )
    interestAccrualListDatacacheRepository.getInterestAccrualList(taxRef, accPeriod, interestType)
  }

}
