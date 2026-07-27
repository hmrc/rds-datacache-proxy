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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdminRule

import java.time.LocalDate

trait AdminRuleHelper {
  val adminRuleWithAllFields: AdminRule = AdminRule(ruleNumber = Some(BigDecimal(986)), ruleDate = Some(LocalDate.of(2026, 7, 24)))
  val adminRuleWithoutRuleNumber: AdminRule = AdminRule(ruleNumber = None, ruleDate = Some(LocalDate.of(2026, 7, 24)))
  val adminRuleWithoutRuleDate: AdminRule = AdminRule(ruleNumber = Some(BigDecimal(345)), ruleDate = None)
  val adminRuleWithEmptyFields: AdminRule = AdminRule(None, None)

}
