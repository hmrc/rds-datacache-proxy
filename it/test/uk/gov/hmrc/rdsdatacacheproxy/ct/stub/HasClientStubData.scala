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

package uk.gov.hmrc.rdsdatacacheproxy.ct.stub

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.HasClient

object HasClientStubData {

  def getHasClient(
    utr: Long,
    irAgentId: String,
    credentialId: String
  ): HasClient =
    utr match {
      case 1 => HasClient(status = 1)
      case 2 => HasClient(status = 0)
      case 67 => throw new Exception("Downstream error")
      case _  => throw new Exception("Unexpected UTR")
    }
}
