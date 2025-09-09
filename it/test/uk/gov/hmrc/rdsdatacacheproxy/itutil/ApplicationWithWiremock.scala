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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames as PlayHeaders
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.HeaderNames

import scala.concurrent.Future


trait ApplicationWithWiremock
  extends AnyWordSpec
    with GuiceOneServerPerSuite
    with BeforeAndAfterAll
    with BeforeAndAfterEach:

  lazy val wireMock = new WireMock

  val extraConfig: Map[String, Any] = {
    Map[String, Any](
      "microservice.services.auth.host" -> WireMockConstants.stubHost,
      "microservice.services.auth.port" -> WireMockConstants.stubPort,
      "feature-switch.cis-rds-stubbed"      -> true
    )
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(extraConfig)
    .build()

  lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]

  override protected def beforeAll(): Unit =
    wireMock.start()
    super.beforeAll()

  override def beforeEach(): Unit =
    wireMock.resetAll()
    super.beforeEach()

  override def afterAll(): Unit =
    wireMock.stop()
    super.afterAll()

  val baseUrl: String = s"http://localhost:$port/rds-datacache-proxy"

  protected def get(uri: String): Future[WSResponse] =
    wsClient.url(s"$baseUrl$uri")
      .withHttpHeaders(PlayHeaders.AUTHORIZATION -> "testId", HeaderNames.xSessionId -> "sessionId")
      .get()

  protected def post(uri: String, body: JsValue): Future[WSResponse] =
    wsClient.url(s"$baseUrl$uri")
      .withHttpHeaders(PlayHeaders.AUTHORIZATION -> "testId", HeaderNames.xSessionId -> "sessionId")
      .post[JsValue](body)