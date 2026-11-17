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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.{AllClients, ClientList}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.manageClient.GetClientsByCrnRepository

import scala.concurrent.Future

class GetClientByCrnServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: GetClientsByCrnRepository = mock[GetClientsByCrnRepository]

    val service = new GetClientByCrnService(mockRepo)

    val irAgentReference = "STN006"
    val credentialId = "78366363"
    val companyRegNo = "REG-67-63"

    val singleClient: ClientList = ClientList(
      utr          = "1234567890",
      companyRegNo = "AB123456",
      companyName  = "Acme Trading Ltd",
      agentOwnRef  = "AGT-REF-001"
    )

    val singleObjectResponse: AllClients = AllClients(
      clientCount          = 1,
      clientList           = List(singleClient),
      clientNameStartChars = List("A")
    )
  }

  "GetClientByCrnService must return AllClients" in new Setup {
    when(mockRepo.getClientByCrn(any(), any(), any()))
      .thenReturn(Future.successful(singleObjectResponse))

    val result: AllClients = service.getClientByCrn(irAgentReference, credentialId, companyRegNo).futureValue

    result mustBe singleObjectResponse

    verify(mockRepo, times(1)).getClientByCrn(any(), any(), any())

  }
  "ReallocationFromAccPeriodService must propagate failure from repository" in new Setup {

    val exception = new RuntimeException("Failure from Repo")

    when(mockRepo.getClientByCrn(any(), any(), any()))
      .thenReturn(Future.failed(exception))

    val result: Throwable = service.getClientByCrn(irAgentReference, credentialId, companyRegNo).failed.futureValue

    result mustBe exception

    verify(mockRepo, times(1)).getClientByCrn(any(), any(), any())

  }

}
