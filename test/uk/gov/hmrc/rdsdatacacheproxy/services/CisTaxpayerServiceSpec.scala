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
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.repositories.CisMonthlyReturnSource

import scala.concurrent.Future

final class CisTaxpayerServiceSpec
  extends SpecBase{

  private val source = mock[CisMonthlyReturnSource]
  private val service = new CisTaxpayerService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  private val ton = "123"
  private val tor = "AB456"

  "CisTaxpayerService#getInstanceIdByTaxReference" - {

    "return CisTaxpayer when the repository returns Some(CisTaxpayer)" in {
      val taxpayer = mkTaxpayer()
      when(source.getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor)))
        .thenReturn(Future.successful(Some(taxpayer)))
      val out = service.getCisTaxpayerByTaxReference(ton, tor).futureValue

      verify(source).getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor))
      verifyNoMoreInteractions(source)
      out mustBe taxpayer
    }

    "fail with NoSuchElementException (including TON/TOR) when the repository returns Some but uniqueId is blank" in {
      val taxpayer = mkTaxpayer().copy(uniqueId = "  ")
      when(source.getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor)))
        .thenReturn(Future.successful(Some(taxpayer)))

      val ex = service.getCisTaxpayerByTaxReference(ton, tor).failed.futureValue
      ex mustBe a[NoSuchElementException]
      ex.getMessage must include("TON=123")
      ex.getMessage must include("TOR=AB456")

      verify(source).getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor))
      verifyNoMoreInteractions(source)
    }

    "fail with NoSuchElementException (including TON/TOR) when the repository returns None" in {
      when(source.getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor)))
        .thenReturn(Future.successful(None))

      val ex = service.getCisTaxpayerByTaxReference(ton, tor).failed.futureValue
      ex mustBe a [NoSuchElementException]
      ex.getMessage must include ("TON=123")
      ex.getMessage must include ("TOR=AB456")

      verify(source).getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("db exploded", 502)

      when(source.getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor)))
        .thenReturn(Future.failed(boom))

      val ex = service.getCisTaxpayerByTaxReference(ton, tor).failed.futureValue
      ex mustBe boom

      verify(source).getCisTaxpayerByTaxRef(eqTo(ton), eqTo(tor))
      verifyNoMoreInteractions(source)
    }
  }
}

