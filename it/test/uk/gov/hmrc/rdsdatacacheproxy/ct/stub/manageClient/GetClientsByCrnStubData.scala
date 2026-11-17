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

package uk.gov.hmrc.rdsdatacacheproxy.ct.stub.manageClient

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.{AllClients, ClientList}

object GetClientsByCrnStubData {

  val emptyResponse: AllClients = AllClients(
    clientCount          = 0,
    clientList           = List.empty,
    clientNameStartChars = List.empty
  )

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

  val multipleClients: List[ClientList] = List(
    ClientList("1234567890", "AB123456", "Acme Trading Ltd", "AGT-REF-001"),
    ClientList("2345678901", "CD234567", "Brightwater Holdings", "AGT-REF-002"),
    ClientList("3456789012", "EF345678", "Bluefield Logistics", "AGT-REF-003"),
    ClientList("4567890123", "GH456789", "Zenith Consulting Ltd", "AGT-REF-004")
  )

  val multipleObjectsResponse: AllClients = AllClients(
    clientCount          = multipleClients.size,
    clientList           = multipleClients,
    clientNameStartChars = multipleClients.map(_.companyName.take(1).toUpperCase).distinct.sorted
  )

  def getClientByCrn(irAgentReference: String, credentialId: String, companyRegNo: String): AllClients = {
    (irAgentReference, credentialId, companyRegNo) match {
      case ("1234", "78783", "YT73")  => emptyResponse
      case ("1234", "783", "Y73")  => singleObjectResponse
      case ("134", "73", "YT3")   => multipleObjectsResponse
      case ("12", "78", "Y") => throw new RuntimeException("Downstream error")
    }
  }

}
