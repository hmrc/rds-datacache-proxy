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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.InvalidRegimeCode

class RegimeSpec extends AnyWordSpec with Matchers {

  "fromString" should {
    "return valid regime" in {
      Regime.fromString("mgd") mustBe Right(Regime.MGD)
      Regime.fromString("gbd") mustBe Right(Regime.GBD)
      Regime.fromString("pbd") mustBe Right(Regime.PBD)
      Regime.fromString("rgd") mustBe Right(Regime.RGD)
    }

    "return valid regime irrespective of case" in {
      Regime.fromString("MGD") mustBe Right(Regime.MGD)
      Regime.fromString("gbD") mustBe Right(Regime.GBD)
      Regime.fromString("pBd") mustBe Right(Regime.PBD)
      Regime.fromString("rGd") mustBe Right(Regime.RGD)
    }

    "return valid regime when value has white spaces" in {
      Regime.fromString("mgd ") mustBe Right(Regime.MGD)
      Regime.fromString("gbd ") mustBe Right(Regime.GBD)
      Regime.fromString("pbd ") mustBe Right(Regime.PBD)
      Regime.fromString("rgd ") mustBe Right(Regime.RGD)
    }

    "return InvalidRegimeCode error when regime is not recognised" in {
      Regime.fromString("unknown") mustBe Left(InvalidRegimeCode)
    }
  }
}
