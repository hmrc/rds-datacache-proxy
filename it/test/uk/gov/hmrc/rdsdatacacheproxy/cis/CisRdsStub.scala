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

package uk.gov.hmrc.rdsdatacacheproxy.cis

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{CisClientSearchResult, CisTaxpayer, SchemePrepop, SubcontractorPrepopRecord}
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

  override def getAllClients(
    irAgentId: String,
    credentialId: String,
    start: Int,
    count: Int,
    sort: Int,
    order: String
  ): Future[CisClientSearchResult] = {
    val irAgentIdExists = Option(irAgentId).exists(_.trim.nonEmpty)
    val credentialIdExists = Option(credentialId).exists(_.trim.nonEmpty)

    if (irAgentIdExists && credentialIdExists) {
      val clients = List(
        stubUtils.createCisTaxpayerSearchResult(uniqueId        = "1",
                                                taxOfficeNumber = "123",
                                                taxOfficeRef    = "AB001",
                                                employerName1   = Some("ABC Construction Ltd")
                                               ),
        stubUtils
          .createCisTaxpayerSearchResult(uniqueId = "2", taxOfficeNumber = "456", taxOfficeRef = "CD002", employerName1 = Some("XYZ Builders")),
        stubUtils
          .createCisTaxpayerSearchResult(uniqueId = "3", taxOfficeNumber = "789", taxOfficeRef = "EF003", employerName1 = Some("Best Contractors"))
      )

      val nameChars = List("A", "B", "X")

      logger.info(
        s"[CIS-STUB] getAllClients -> IR_AGENT_ID=${irAgentId.trim}, CREDENTIAL_ID=${credentialId.trim}, START=$start, COUNT=$count => ${clients.length} clients"
      )

      Future.successful(
        CisClientSearchResult(
          clients                      = clients,
          totalCount                   = clients.length,
          clientNameStartingCharacters = nameChars
        )
      )
    } else {
      logger.warn(
        s"[CIS-STUB] getAllClients -> missing/blank IR_AGENT_ID/CREDENTIAL_ID: IR_AGENT_ID=${Option(irAgentId).map(_.trim).getOrElse("")}, CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}"
      )
      Future.successful(
        CisClientSearchResult(
          clients                      = List.empty,
          totalCount                   = 0,
          clientNameStartingCharacters = List.empty
        )
      )
    }
  }

  override def hasClient(
    irAgentId: String,
    credentialId: String,
    taxOfficeNumber: String,
    taxOfficeReference: String
  ): Future[Boolean] = {
    val allPresent = List(
      irAgentId,
      credentialId,
      taxOfficeNumber,
      taxOfficeReference
    ).forall(_.trim.nonEmpty)

    if (allPresent) {
      val clientExists = (taxOfficeNumber.trim, taxOfficeReference.trim) match {
        case ("123", "AB001") => true
        case ("456", "CD002") => true
        case ("789", "EF003") => true
        case _                => false
      }

      logger.info(
        s"[CIS-STUB] hasClient -> IR_AGENT_ID=${irAgentId.trim}, CREDENTIAL_ID=${credentialId.trim}, TON=${taxOfficeNumber.trim}, TOR=${taxOfficeReference.trim} => exists=$clientExists"
      )

      Future.successful(clientExists)
    } else {
      logger.warn(
        s"[CIS-STUB] hasClient -> missing/blank parameters: IR_AGENT_ID=${Option(irAgentId).map(_.trim).getOrElse("")}, CREDENTIAL_ID=${Option(credentialId).map(_.trim).getOrElse("")}, TON=${Option(taxOfficeNumber).map(_.trim).getOrElse("")}, TOR=${Option(taxOfficeReference).map(_.trim).getOrElse("")}"
      )
      Future.successful(false)
    }
  }


  override def getSchemePrepopByKnownFacts(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    agentOwnReference: String
  ): Future[Option[SchemePrepop]] = {
    if (taxOfficeNumber.trim.nonEmpty && taxOfficeReference.trim.nonEmpty && agentOwnReference.trim.nonEmpty) {
      val scheme = SchemePrepop(
        taxOfficeNumber    = taxOfficeNumber.trim,
        taxOfficeReference = taxOfficeReference.trim,
        agentOwnReference  = agentOwnReference.trim,
        utr                = Some("1123456789"),
        schemeName         = "PAL-355 Scheme"
      )
      Future.successful(Some(scheme))
    } else {
      Future.successful(None)
    }
  }

  override def getSubcontractorsPrepopByKnownFacts(
    taxOfficeNumber: String,
    taxOfficeReference: String,
    agentOwnReference: String
  ): Future[Seq[SubcontractorPrepopRecord]] = {
    if (taxOfficeNumber.trim.nonEmpty && taxOfficeReference.trim.nonEmpty && agentOwnReference.trim.nonEmpty) {
      val subcontractor = SubcontractorPrepopRecord(
        subcontractorType  = "I",
        subcontractorUtr   = "1234567890",
        verificationNumber = "12345678901",
        verificationSuffix = Some("AB"),
        title              = Some("Mr"),
        firstName          = Some("Test"),
        secondName         = None,
        surname            = Some("Company"),
        tradingName        = Some("Test Company Ltd")
      )
      Future.successful(Seq(subcontractor))
    } else {
      Future.successful(Seq.empty)
    }
  }
}
