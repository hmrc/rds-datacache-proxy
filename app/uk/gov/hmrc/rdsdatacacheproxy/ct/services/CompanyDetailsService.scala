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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services

import play.api.Logging
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CompanyDetails, CompanyDetailsResponse}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.CompanyDetailsRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyDetailsService @Inject() (repository: CompanyDetailsRepository)(implicit ec: ExecutionContext) extends Logging {
  def getCompanyDetails(taxPayerReference: Long): Future[CompanyDetailsResponse] = {
    logger.info(s"Calling repository for taxRef: $taxPayerReference")
    repository.getCompanyDetails(taxPayerReference).map { value =>
      CompanyDetailsResponse(
        taxpayerDetails = value.taxpayerDetails.map { details =>
          CompanyDetails(
            orgUnitId    = details.orgUnitId.trim,
            companyName  = details.companyName.trim,
            companyRegNo = details.companyRegNo.map(_.trim),
            addressLine1 = details.addressLine1.map(_.trim),
            addressLine2 = details.addressLine2.map(_.trim),
            addressLine3 = details.addressLine3.map(_.trim),
            addressLine4 = details.addressLine4.map(_.trim),
            postCode     = details.postCode.map(_.trim)
          )
        }
      )
    }
  }
}
