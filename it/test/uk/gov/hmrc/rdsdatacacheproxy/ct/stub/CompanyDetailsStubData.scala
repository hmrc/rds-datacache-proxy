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

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CompanyDetailsResponse, CompanyDetails}


object CompanyDetailsStubData {

  val emptyCompanyResponse: CompanyDetailsResponse = CompanyDetailsResponse(taxpayerDetails = List.empty)

  val singleCompanyResponse: CompanyDetailsResponse = CompanyDetailsResponse(
    taxpayerDetails = List(
      CompanyDetails(
        orgUnitId    = "OU-100001",
        companyName  = "Acme Widgets Ltd",
        companyRegNo = Some("01234567"),
        addressLine1 = Some("12 High Street"),
        addressLine2 = Some("Dudley"),
        addressLine3 = Some("West Midlands"),
        addressLine4 = Some("England"),
        postCode     = Some("DY1 1AA")
      )
    )
  )

  val multipleCompaniesResponse: CompanyDetailsResponse = CompanyDetailsResponse(
    taxpayerDetails = List(
      CompanyDetails(
        orgUnitId    = "OU-100001",
        companyName  = "Acme Widgets Ltd",
        companyRegNo = Some("01234567"),
        addressLine1 = Some("12 High Street"),
        addressLine2 = Some("Dudley"),
        addressLine3 = Some("West Midlands"),
        addressLine4 = Some("England"),
        postCode     = Some("DY1 1AA")
      ),
      CompanyDetails(
        orgUnitId    = "OU-100002",
        companyName  = "Blackcountry Engineering plc",
        companyRegNo = Some("07654321"),
        addressLine1 = Some("Unit 5, Castle Industrial Estate"),
        addressLine2 = Some("Tipton"),
        addressLine3 = None,
        addressLine4 = None,
        postCode     = Some("DY4 8RT")
      ),
      CompanyDetails(
        orgUnitId    = "OU-100003",
        companyName  = "Greenfield Consulting LLP",
        companyRegNo = None,
        addressLine1 = Some("1 Market Place"),
        addressLine2 = None,
        addressLine3 = None,
        addressLine4 = None,
        postCode     = Some("B1 2JP")
      )
    )
  )

  def getCompanyDetails(taxRef: Long): CompanyDetailsResponse = {
    taxRef match {
      case 10L  => singleCompanyResponse
      case 20L  => multipleCompaniesResponse
      case 2L   => emptyCompanyResponse
      case 200L => throw new RuntimeException("Downstream error")
    }
  }

}
