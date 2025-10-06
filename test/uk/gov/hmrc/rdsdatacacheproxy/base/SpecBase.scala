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

package uk.gov.hmrc.rdsdatacacheproxy.base

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TestSuite, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, PlayBodyParsers}
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{DefaultAwaitTimeout, FakeHeaders, FakeRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.rdsdatacacheproxy.actions.FakeAuthAction
import uk.gov.hmrc.rdsdatacacheproxy.models.CisTaxpayer

import scala.concurrent.ExecutionContext

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with DefaultAwaitTimeout
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach
    with TestSuite
    with FakeApplicationFactory
    with BaseOneAppPerSuite {

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .build()

  val cc: ControllerComponents = stubControllerComponents()
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val bodyParsers: PlayBodyParsers = app.injector.instanceOf[PlayBodyParsers]
  val fakeAuthAction = new FakeAuthAction(bodyParsers)

  def fakeRequestWithJsonBody(json: JsValue): FakeRequest[JsValue] = fakeRequestWithBody(json)
  def fakeRequestWithBody[A](body: A): FakeRequest[A] = FakeRequest("", "/", FakeHeaders(), body)

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  final val TonHeader = "X-Tax-Office-Number"
  final val TorHeader = "X-Tax-Office-Reference"

  def requestWithCisHeaders(
                             ton: String = "123",
                             tor: String = "AB456"
                           ): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders(TonHeader -> ton, TorHeader -> tor)

  def requestMissingTon(tor: String = "AB456"): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders(TorHeader -> tor)

  def requestMissingTor(ton: String = "123"): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders(TonHeader -> ton)

  def requestWithoutCisHeaders: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest()

  def mkTaxpayer(
                  id: String = "CIS-123",
                  ton: String = "123",
                  tor: String = "AB456",
                  employerName1: Option[String] = Some("TEST LTD")
                ): CisTaxpayer =
    CisTaxpayer(
      uniqueId = id,
      taxOfficeNumber = ton,
      taxOfficeRef = tor,
      aoDistrict = None,
      aoPayType = None,
      aoCheckCode = None,
      aoReference = None,
      validBusinessAddr = None,
      correlation = None,
      ggAgentId = None,
      employerName1 = employerName1,
      employerName2 = None,
      agentOwnRef = None,
      schemeName = None,
      utr = None,
      enrolledSig = None
    )
}
