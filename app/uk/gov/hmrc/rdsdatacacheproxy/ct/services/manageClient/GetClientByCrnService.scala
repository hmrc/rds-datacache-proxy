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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.manageClient

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.AllClients
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.GetClientsByCrnRepository

import javax.inject.Inject
import scala.concurrent.Future

class GetClientByCrnService @Inject() (repo: GetClientsByCrnRepository) extends Logging {

  def getClientByCrn(irAgentReference: String, credentialId: String, companyRegNo: String): Future[AllClients] = {
    logger.info(
      s"Calling repository to fetch Client details using companyRegNo : $companyRegNo"
    )
    repo.getClientByCrn(irAgentReference, credentialId, companyRegNo)
  }

}
