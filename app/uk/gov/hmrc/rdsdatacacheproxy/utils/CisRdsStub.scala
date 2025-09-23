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

package uk.gov.hmrc.rdsdatacacheproxy.utils

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.repositories.CisMonthlyReturnSource

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CisRdsStub @Inject()() extends CisMonthlyReturnSource with Logging {
  
  override def getInstanceIdByTaxRef(taxOfficeNumber: String, taxOfficeReference: String): Future[Option[String]] = {
    val tonOk = Option(taxOfficeNumber).exists(_.trim.nonEmpty)
    val torOk = Option(taxOfficeReference).exists(_.trim.nonEmpty)

    if (tonOk && torOk) {
      logger.info(s"[CIS-STUB] findInstanceId -> TON=$taxOfficeNumber, TOR=$taxOfficeReference => instanceId=1")
      Future.successful(Some("1"))
    } else {
      logger.warn(s"[CIS-STUB] findInstanceId -> missing/blank TON/TOR: ton='$taxOfficeNumber', tor='$taxOfficeReference'")
      Future.successful(None)
    }
  }
}