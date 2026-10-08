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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CompanyDetails, CompanyDetailsResponse}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.CompanyDetailsRepositoryImpl

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyDetailsServiceSpec extends AnyWordSpec with Matchers {

  private trait Setup {
    val mockRepository: CompanyDetailsRepositoryImpl = mock[CompanyDetailsRepositoryImpl]

    val service = new CompanyDetailsService(mockRepository)
    val taxRef = 123456L

    val exampleResponseWithSpaces: CompanyDetailsResponse = CompanyDetailsResponse(
      taxpayerDetails = List(
        CompanyDetails(
          orgUnitId    = "  OU-100234   ",
          companyName  = "   Brightwater Engineering Ltd    ",
          companyRegNo = Some("  09876543 "),
          addressLine1 = Some("  Unit 4, Riverside Park"),
          addressLine2 = Some("  Canal Street"),
          addressLine3 = Some("Brierley Hill   "),
          addressLine4 = Some("   West Midlands  "),
          postCode     = Some("  DY5 1XY")
        ),
        CompanyDetails(
          orgUnitId    = "  OU-100587",
          companyName  = "   Northgate Trading Co",
          companyRegNo = Some("                   "),
          addressLine1 = Some("  12 Market Square   "),
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postCode     = Some("   B1 2AB  ")
        )
      )
    )
    val exampleResponseWithSpacesTrimmed: CompanyDetailsResponse = CompanyDetailsResponse(
      taxpayerDetails = List(
        CompanyDetails(
          orgUnitId    = "OU-100234",
          companyName  = "Brightwater Engineering Ltd",
          companyRegNo = Some("09876543"),
          addressLine1 = Some("Unit 4, Riverside Park"),
          addressLine2 = Some("Canal Street"),
          addressLine3 = Some("Brierley Hill"),
          addressLine4 = Some("West Midlands"),
          postCode     = Some("DY5 1XY")
        ),
        CompanyDetails(
          orgUnitId    = "OU-100587",
          companyName  = "Northgate Trading Co",
          companyRegNo = Some(""),
          addressLine1 = Some("12 Market Square"),
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postCode     = Some("B1 2AB")
        )
      )
    )
  }

  "CompanyDetailsService" should {

    "return CompanyDetails Response from repository trimming all leading a trailing spaces in the fields" in new Setup {
      when(mockRepository.getCompanyDetails(any())).thenReturn(Future.successful(exampleResponseWithSpaces))

      val result: CompanyDetailsResponse = service.getCompanyDetails(taxRef).futureValue

      result shouldBe exampleResponseWithSpacesTrimmed

      verify(mockRepository).getCompanyDetails(any())
      verify(mockRepository, times(1)).getCompanyDetails(any())

    }

    "propagate exception from Repository " in new Setup {
      val ex = new RuntimeException("Unexpected failure")
      when(mockRepository.getCompanyDetails(any())).thenReturn(Future.failed(ex))

      val result: Throwable = service.getCompanyDetails(taxRef).failed.futureValue

      result shouldBe ex

      result.getMessage should include("Unexpected failure")

      verify(mockRepository).getCompanyDetails(any())
      verify(mockRepository, times(1)).getCompanyDetails(any())
    }
  }

}
