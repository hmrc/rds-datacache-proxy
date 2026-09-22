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

package uk.gov.hmrc.rdsdatacacheproxy.ct.stub

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CompanyNominator

object CompanyNominatorStubData {

  val companyNominatorTrue: CompanyNominator = CompanyNominator(isParticipator = "Y")

  val companyNominatorFalse: CompanyNominator = CompanyNominator(isParticipator = "N")
  
  
  def getIsCompanyNominatorOfGPA(gpaUtr: Long, nominatedCompanyUtr: Long): CompanyNominator = {
    gpaUtr match {
      case 10L  => companyNominatorTrue
      case 20L  => companyNominatorFalse
      case 200L => throw new RuntimeException("Downstream error")
      case _    => companyNominatorTrue
    }
  }

}
