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

package uk.gov.hmrc.rdsdatacacheproxy.shared.utils

import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.errors.StatementError.{InvalidRegNumber, InvalidRegimeCode}

class GRNValidatorSpec extends SpecBase {

  "ValidationUtil validateRegime" - {
    "validateRegime returns TRUE for GBD" in {
      GRNValidator.validateRegime(Regime.GBD, "XBA00003000000", "test") mustBe Right(())
      GRNValidator.validateRegime(Regime.GBD, "XBA00003199999", "test") mustBe Right(())
    }

    "validateRegime returns FALSE for GBD" in {
      GRNValidator.validateRegime(Regime.GBD, "XBA00002999999", "test") mustBe Left(InvalidRegimeCode)
      GRNValidator.validateRegime(Regime.GBD, "XBA00003200000", "test") mustBe Left(InvalidRegimeCode)
      GRNValidator.validateRegime(Regime.PBD, "XBA00003199999", "test") mustBe Left(InvalidRegimeCode)
    }

    "validateRegime returns TRUE for PBD" in {
      GRNValidator.validateRegime(Regime.PBD, "XBA00003200000", "test") mustBe Right(())
      GRNValidator.validateRegime(Regime.PBD, "XBA00003399999", "test") mustBe Right(())
    }

    "validateRegime returns FALSE for PBD" in {
      GRNValidator.validateRegime(Regime.PBD, "XBA00003199999", "test") mustBe Left(InvalidRegimeCode)
      GRNValidator.validateRegime(Regime.PBD, "XBA00003400000", "test") mustBe Left(InvalidRegimeCode)
      GRNValidator.validateRegime(Regime.GBD, "XBA00003200000", "test") mustBe Left(InvalidRegimeCode)
    }

    "validateRegime returns TRUE for RGD" in {
      GRNValidator.validateRegime(Regime.RGD, "XBA00003400000", "test") mustBe Right(())
      GRNValidator.validateRegime(Regime.RGD, "XBA00003599999", "test") mustBe Right(())
    }

    "validateRegime returns FALSE for RGD" in {
      GRNValidator.validateRegime(Regime.RGD, "XBA00003399999", "test") mustBe Left(InvalidRegimeCode)
      GRNValidator.validateRegime(Regime.RGD, "XBA00003600000", "test") mustBe Left(InvalidRegimeCode)
      GRNValidator.validateRegime(Regime.GBD, "XBA00003400000", "test") mustBe Left(InvalidRegimeCode)
    }

    "validateRegime returns TRUE for MGD" in {
      GRNValidator.validateRegime(Regime.MGD, "XBA00000400000", "test") mustBe Right(())
      GRNValidator.validateRegime(Regime.MGD, "XBA00003500000", "test") mustBe Right(())
    }

    "validateRegime returns FALSE for short RegNums" in {
      GRNValidator.validateRegime(Regime.GBD, "XBA0002999999", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegime(Regime.GBD, "XBA123", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegime(Regime.PBD, "XBA", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegime returns FALSE for Reg Nums with spaces" in {
      GRNValidator.validateRegime(Regime.GBD, " WA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegime(Regime.GBD, "X A00003199999", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegime(Regime.GBD, "XNA0000 200000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegime(Regime.GBD, "XEA000034000 0", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegime(Regime.GBD, "XGM0000312220 ", "test") mustBe Left(InvalidRegNumber)
    }
  }

  "ValidationUtil validateRegNum" - {
    "validateRegNum returns TRUE for valid Reg Nums" in {
      GRNValidator.validateRegNum("XWA00003000000", "test") mustBe Right(()) // GBD
      GRNValidator.validateRegNum("XHA00003199999", "test") mustBe Right(()) // GBD
      GRNValidator.validateRegNum("XNA00003200000", "test") mustBe Right(()) // PBD
      GRNValidator.validateRegNum("XEA00003400000", "test") mustBe Right(()) // RGD
      GRNValidator.validateRegNum("XGM00003122200", "test") mustBe Right(()) // MGD
    }

    "validateRegNum returns FALSE for invalid Check Digit" in {
      GRNValidator.validateRegNum("XZA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum("XZA00003199999", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for too short" in {
      GRNValidator.validateRegNum("XWA0003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for very short" in {
      GRNValidator.validateRegNum("XWA001", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for too long" in {
      GRNValidator.validateRegNum("XWA000003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for does not match regEx" in {
      GRNValidator.validateRegNum("XWA0000300000Z", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum("1WA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum("XW000003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for Reg Nums with spaces" in {
      GRNValidator.validateRegNum(" WA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum("X A00003199999", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum("XNA0000 200000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum("XEA000034000 0", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum("XGM0000312220 ", "test") mustBe Left(InvalidRegNumber)
    }
  }
}
