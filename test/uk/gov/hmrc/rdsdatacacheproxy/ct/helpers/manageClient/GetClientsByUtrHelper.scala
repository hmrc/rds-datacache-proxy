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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.manageClient

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient.{AllClients, ClientList}

trait GetClientsByUtrHelper {

  val emptyClients: AllClients = AllClients(
    clientCount          = 0,
    clientList           = List.empty,
    clientNameStartChars = List.empty
  )

  val singleClient: AllClients = AllClients(
    clientCount = 1,
    clientList = List(
      ClientList(
        utr          = "1234567890",
        companyRegNo = Some("01234A"),
        companyName  = "ABC Limited",
        agentOwnRef  = Some("ABC10")
      )
    ),
    clientNameStartChars = List("A")
  )

  val multipleClients: AllClients = AllClients(
    clientCount = 2,
    clientList = List(
      ClientList(
        utr          = "2345678901",
        companyRegNo = Some("01234A"),
        companyName  = "ABC Limited",
        agentOwnRef  = Some("ABC10")
      ),
      ClientList(
        utr          = "356789012",
        companyRegNo = Some("6789B"),
        companyName  = "DEF Limited",
        agentOwnRef  = Some("DEF10")
      )
    ),
    clientNameStartChars = List("A")
  )

  val multipleNameStartChars: AllClients = AllClients(
    clientCount = 1,
    clientList = List(
      ClientList(
        utr          = "4567890123",
        companyRegNo = Some("111213C"),
        companyName  = "GHI Limited",
        agentOwnRef  = Some("GHI10")
      )
    ),
    clientNameStartChars = List("G", "H", "I")
  )
}
