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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ClientDownloadStatus


object ClientListDownloadStatusStubData {
  
  val initiateDownload: Int = -1
  val inProgress: Int = 0
  val succeeded: Int = 1
  val failed: Int = 2
  
  def getClientListDownloadStatus(   credentialId: String,
                                     serviceName: String,
                                     gracePeriod: Int): Int = {
    credentialId match {
      case "1"  => initiateDownload
      case "2"  => inProgress
      case "3"  => succeeded
      case "4"  => failed
      case "99"  => throw new RuntimeException("Downstream error")
    }
  }

}
