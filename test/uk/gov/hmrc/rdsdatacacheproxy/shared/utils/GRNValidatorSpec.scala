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

  "ValidationUtil validateRegNum for GTR Regimes" - {
    "validateRegNum returns TRUE for valid Reg Nums" in {
      GRNValidator.validateRegNum(Regime.GBD, "XWA00003000000", "test") mustBe Right(()) // GBD
      GRNValidator.validateRegNum(Regime.GBD, "XCA00003199999", "test") mustBe Right(()) // GBD
      GRNValidator.validateRegNum(Regime.PBD, "XNA00003200000", "test") mustBe Right(()) // PBD
      GRNValidator.validateRegNum(Regime.PBD, "XWA00003200111", "test") mustBe Right(()) // PBD
      GRNValidator.validateRegNum(Regime.RGD, "XEA00003400000", "test") mustBe Right(()) // RGD
      GRNValidator.validateRegNum(Regime.RGD, "XWA00003400222", "test") mustBe Right(()) // RGD
    }

    "validateRegNum returns FALSE for invalid Check Digit" in {
      GRNValidator.validateRegNum(Regime.GBD, "XZA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.GBD, "XZA00003199999", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for too short" in {
      GRNValidator.validateRegNum(Regime.GBD, "XWA0003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for very short" in {
      GRNValidator.validateRegNum(Regime.GBD, "XWA001", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for too long" in {
      GRNValidator.validateRegNum(Regime.GBD, "XWA000003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for does not match regEx" in {
      GRNValidator.validateRegNum(Regime.GBD, "XWA0000300000Z", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.GBD, "1WA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.GBD, "XW000003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for Reg Nums with spaces" in {
      GRNValidator.validateRegNum(Regime.GBD, " WA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.GBD, "X A00003199999", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.GBD, "XNA0000 200000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.GBD, "XEA000034000 0", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.GBD, "XGM0000312220 ", "test") mustBe Left(InvalidRegNumber)
    }
  }

  "ValidationUtil validateRegNum for MGD Regime" - {
    "validateRegNum returns TRUE for valid Reg Nums" in {
      GRNValidator.validateRegNum(Regime.MGD, "XYM00000000000", "test") mustBe Right(()) // MGD
      GRNValidator.validateRegNum(Regime.MGD, "XAM00000001414", "test") mustBe Right(()) // MGD
      GRNValidator.validateRegNum(Regime.MGD, "XEM00000000640", "test") mustBe Right(()) // MGD
      GRNValidator.validateRegNum(Regime.MGD, "XVM00000000495", "test") mustBe Right(()) // MGD
      GRNValidator.validateRegNum(Regime.MGD, "XHM00000000785", "test") mustBe Right(()) // MGD
    }

    "validateRegNum returns FALSE for too short" in {
      GRNValidator.validateRegNum(Regime.MGD, "XWA0003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for very short" in {
      GRNValidator.validateRegNum(Regime.MGD, "XWA001", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for too long" in {
      GRNValidator.validateRegNum(Regime.MGD, "XWA000003000000", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for does not match regEx" in {
      GRNValidator.validateRegNum(Regime.MGD, "XWA0000300000Z", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XWM0000300000Z", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "1WA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "1WM00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XW000003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XZA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XZA00003199999", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XAZ00001239456", "test") mustBe Left(InvalidRegNumber)
    }

    "validateRegNum returns FALSE for Reg Nums with spaces" in {
      GRNValidator.validateRegNum(Regime.MGD, " WA00003000000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "X A00003199999", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XNA0000 200000", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XEA000034000 0", "test") mustBe Left(InvalidRegNumber)
      GRNValidator.validateRegNum(Regime.MGD, "XGM0000312220 ", "test") mustBe Left(InvalidRegNumber)
    }
  }

  "GRNValidator validateRegNum for RegNos that we know are VALID in production" - {
    "validateRegimeAndRegNo returns TRUE for GTR" in {
      GRNValidator.validateRegNum(Regime.GBD, "XYM00003001213", "test") mustBe Right(()) // GBD
      GRNValidator.validateRegNum(Regime.GBD, "XTM00003000512", "test") mustBe Right(()) // GBD
      GRNValidator.validateRegNum(Regime.GBD, "XKM00003000195", "test") mustBe Right(()) // GBD
      GRNValidator.validateRegNum(Regime.PBD, "XKM00003200218", "test") mustBe Right(()) // PBD
      GRNValidator.validateRegNum(Regime.PBD, "XSM00003200104", "test") mustBe Right(()) // PBD
      GRNValidator.validateRegNum(Regime.PBD, "XSM00003200290", "test") mustBe Right(()) // PBD
      GRNValidator.validateRegNum(Regime.RGD, "XGM00003400594", "test") mustBe Right(()) // RGD
      GRNValidator.validateRegNum(Regime.RGD, "XQM00003400116", "test") mustBe Right(()) // RGD
      GRNValidator.validateRegNum(Regime.RGD, "XVM00003400600", "test") mustBe Right(()) // RGD
    }
    "validateRegimeAndRegNo returns TRUE for MGD" in {
      GRNValidator.validateRegNum(Regime.MGD, "XAM00000001414", "test") mustBe Right(()) // MGD
      GRNValidator.validateRegNum(Regime.MGD, "XEM00000000640", "test") mustBe Right(()) // MGD
      GRNValidator.validateRegNum(Regime.MGD, "XVM00000000495", "test") mustBe Right(()) // MGD
      GRNValidator.validateRegNum(Regime.MGD, "XHM00000000785", "test") mustBe Right(()) // MGD
    }
  }
}
