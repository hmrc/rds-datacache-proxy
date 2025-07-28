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

package uk.gov.hmrc.rdsdatacacheproxy.itutil

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{WireMock => WireMockClient}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

object WireMockConstants:
  val stubPort = 11111
  val stubHost = "localhost"

class WireMock:
  val stubPort = 11111
  val stubHost = "localhost"

  var wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(WireMockConstants.stubPort))

  def start(): Unit =
    if !wireMockServer.isRunning then
      wireMockServer.start()
      WireMockClient.configureFor(WireMockConstants.stubHost, WireMockConstants.stubPort)

  def stop(): Unit =
    wireMockServer.stop()

  def resetAll(): Unit =
    wireMockServer.resetMappings()
    wireMockServer.resetRequests()
