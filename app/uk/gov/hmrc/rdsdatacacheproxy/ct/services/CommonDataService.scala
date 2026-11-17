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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CommonData
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.CommonDataRepository

import javax.inject.Inject
import scala.concurrent.Future

class CommonDataService @Inject() (repository: CommonDataRepository) extends Logging {

  def getCommonData(): Future[CommonData] = {
    logger.info("Calling Get Common Data for CT Core repository")
    repository.getCommonData()
  }
}
