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

package uk.gov.hmrc.rdsdatacacheproxy.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.repositories.CisMonthlyReturnSource

import scala.concurrent.{ExecutionContext, Future}

final class CisTaxpayerServiceSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar
    with BeforeAndAfterEach {

  private val source = mock[CisMonthlyReturnSource]
  implicit val ec: ExecutionContext = ExecutionContext.global
  private val service = new CisTaxpayerService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  private val ton = "123"
  private val tor = "AB456"

  "CisTaxpayerService#getInstanceIdByTaxReference" should {

    "return instanceId when the repository returns Some(id)" in {
      when(source.getInstanceIdByTaxRef(eqTo(ton), eqTo(tor)))
        .thenReturn(Future.successful(Some("inst-1")))
      val out = service.getInstanceIdByTaxReference(ton, tor).futureValue

      verify(source).getInstanceIdByTaxRef(eqTo(ton), eqTo(tor))
      verifyNoMoreInteractions(source)
      out mustBe "inst-1"
    }

    "fail with NoSuchElementException (including TON/TOR) when the repository returns None" in {
      when(source.getInstanceIdByTaxRef(eqTo(ton), eqTo(tor)))
        .thenReturn(Future.successful(None))

      val ex = service.getInstanceIdByTaxReference(ton, tor).failed.futureValue
      ex mustBe a [NoSuchElementException]
      ex.getMessage should include ("TON=123")
      ex.getMessage should include ("TOR=AB456")

      verify(source).getInstanceIdByTaxRef(eqTo(ton), eqTo(tor))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("db exploded", 502)

      when(source.getInstanceIdByTaxRef(eqTo(ton), eqTo(tor)))
        .thenReturn(Future.failed(boom))

      val ex = service.getInstanceIdByTaxReference(ton, tor).failed.futureValue
      ex mustBe boom

      verify(source).getInstanceIdByTaxRef(eqTo(ton), eqTo(tor))
      verifyNoMoreInteractions(source)
    }
  }
}

