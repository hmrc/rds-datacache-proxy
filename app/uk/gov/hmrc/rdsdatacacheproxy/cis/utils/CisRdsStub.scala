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

package uk.gov.hmrc.rdsdatacacheproxy.cis.utils

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.CisTaxpayer
import uk.gov.hmrc.rdsdatacacheproxy.cis.repositories.CisMonthlyReturnSource

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CisRdsStub @Inject() (stubUtils: StubUtils) extends CisMonthlyReturnSource with Logging {

  override def getCisTaxpayerByTaxRef(
    taxOfficeNumber: String,
    taxOfficeReference: String
  ): Future[Option[CisTaxpayer]] = {
    val ton = Option(taxOfficeNumber).exists(_.trim.nonEmpty)
    val tor = Option(taxOfficeReference).exists(_.trim.nonEmpty)

    (ton, tor) match {
      case (true, true) =>
        val taxpayer = stubUtils.createCisTaxpayer()
        logger.info(
          s"[CIS-STUB] getCisTaxpayerByTaxRef -> TON=${taxOfficeNumber.trim}, TOR=${taxOfficeReference.trim} => uniqueId=${taxpayer.uniqueId}"
        )
        Future.successful(Some(taxpayer))

      case _ =>
        logger.warn(s"[CIS-STUB] getCisTaxpayerByTaxRef -> missing/blank TON/TOR: ton='$taxOfficeNumber', tor='$taxOfficeReference'")
        Future.successful(None)
    }
  }

  override def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int): Future[Int] = {
    val credentialIdExists = Option(credentialId).exists(_.trim.nonEmpty)
    val serviceNameExists = Option(serviceName).exists(_.trim.nonEmpty)

    if (credentialIdExists && serviceNameExists) {
      logger.info(
        s"[CIS-STUB] getClientListDownloadStatus -> CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, SERVICE_NAME=${Option(serviceName).map(_.trim).getOrElse("")} => status=1"
      )
      Future.successful(1)
    } else {
      logger.warn(
        s"[CIS-STUB] getClientListDownloadStatus -> missing/blank CREDENTIAL_ID/SERVICE_NAME: CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, SERVICE_NAME=${Option(serviceName).map(_.trim).getOrElse("")} "
      )
      Future.successful(2)
    }
  }
}
