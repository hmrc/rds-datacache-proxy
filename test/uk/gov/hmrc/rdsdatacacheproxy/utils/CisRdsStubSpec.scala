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

package uk.gov.hmrc.rdsdatacacheproxy.utils

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.models.CisTaxpayer

final class CisRdsStubSpec extends AnyWordSpec with Matchers with ScalaFutures {

  private val utils = new StubUtils()
  private val stub = new CisRdsStub(utils)

  "CisRdsStub#getCisTaxpayerByTaxRef" should {

    "return Some(CisTaxpayer) with uniqueId='1' when TON and TOR are non-empty" in {
      val r1 = stub.getCisTaxpayerByTaxRef("123", "ABC").futureValue
      val tp1 = r1.getOrElse(fail("expected Some(CisTaxpayer)"))

      tp1.uniqueId mustBe "1"
      tp1.taxOfficeNumber mustBe "123"
      tp1.taxOfficeRef mustBe "AB456"
      tp1.employerName1 mustBe Some("TEST LTD")
    }

    "return None when TON is blank" in {
      stub.getCisTaxpayerByTaxRef("   ", "ABC").futureValue mustBe None
    }

    "return None when TOR is blank" in {
      stub.getCisTaxpayerByTaxRef("123", "   ").futureValue mustBe None
    }

    "return None when both are null" in {
      stub.getCisTaxpayerByTaxRef(null, null).futureValue mustBe None
    }
  }
}
