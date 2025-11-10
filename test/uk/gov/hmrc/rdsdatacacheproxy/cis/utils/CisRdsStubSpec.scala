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

package uk.gov.hmrc.rdsdatacacheproxy.cis.utils

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.CisTaxpayer

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

  "CisRdsStub#getClientListDownloadStatus" should {

    "return 1 (Succeeded) when credentialId and serviceName are non-empty" in {
      val result = stub.getClientListDownloadStatus("cred-123", "service-xyz").futureValue
      result mustBe 1
    }

    "return 1 (Succeeded) when credentialId and serviceName have whitespace but are not blank" in {
      val result = stub.getClientListDownloadStatus("  cred-123  ", "  service-xyz  ").futureValue
      result mustBe 1
    }

    "return 2 (Failed) when credentialId is blank" in {
      val result = stub.getClientListDownloadStatus("   ", "service-xyz").futureValue
      result mustBe 2
    }

    "return 2 (Failed) when serviceName is blank" in {
      val result = stub.getClientListDownloadStatus("cred-123", "   ").futureValue
      result mustBe 2
    }

    "return 2 (Failed) when both credentialId and serviceName are blank" in {
      val result = stub.getClientListDownloadStatus("   ", "   ").futureValue
      result mustBe 2
    }

    "handle different grace period values correctly" in {
      val result1 = stub.getClientListDownloadStatus("cred-123", "service-xyz", 7200).futureValue
      result1 mustBe 1

      val result2 = stub.getClientListDownloadStatus("cred-123", "service-xyz", 0).futureValue
      result2 mustBe 1
    }
  }
}
