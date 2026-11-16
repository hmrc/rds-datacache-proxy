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



object AllClientsStubData {

  
  val allClients: AllClients = AllClients(
    clientCount = 100,
    clientList = List(
      ClientList(
      utr = "12345",
      companyRegNo = "01234A",
      companyName = "ABC Limited",
      agentOwnRef = "ABC10"
      )
    ),
    clientNameChars = List(
      "A"
    )
  )

  val multipleClientListAllClients: AllClients = AllClients(
    clientCount = 100,
    clientList = List(
      ClientList(
      utr = "12345",
      companyRegNo = "01234A",
      companyName = "ABC Limited",
      agentOwnRef = "ABC10"
    ),
      ClientList(
        utr = "67890",
        companyRegNo = "6789B",
        companyName = "DEF Limited",
        agentOwnRef = "DEF10"
      )
    ),
    clientNameChars = List(
      "D"
    )
  )

  val multipleItemsClientNameAllClients: AllClients = AllClients(
    clientCount = 100,
    clientList = List(
      ClientList(
      utr = "111213",
      companyRegNo = "111213C",
      companyName = "GHI Limited",
      agentOwnRef = "GHI10"
      )
    ),
    clientNameChars = List(
      "G", "H", "I"
    )
  )

  def getAllClients(irAgentId: String, credentialId: String, start: Int, count: Int, sort: Int, order: String): AllClients = {
    irAgentId match {
      case "irAgentId1"  => allClients
      case "irAgentId2"  => multipleClientListAllClients
      case "irAgentId3"  => multipleItemsClientNameAllClients
      case _ => throw new RuntimeException("Downstream error")
    }
  }

}
