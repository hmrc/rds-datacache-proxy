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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.HasClient
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.HasClientRepository

import javax.inject.Inject
import scala.concurrent.Future

class HasClientService @Inject() (repository: HasClientRepository) extends Logging {

  def hasClient(utr: Long, irAgentId: String, credentialId: String): Future[HasClient] = {
    logger.info(s"Calling Has Client for CT Core repository with UTR: $utr")
    repository.hasClient(utr, irAgentId, credentialId)
  }
}
