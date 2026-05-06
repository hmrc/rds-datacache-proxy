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

package uk.gov.hmrc.rdsdatacacheproxy.gambling

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{BusinessDetails, BusinessName, GamblingStubData, OperatorDetails, MgdCertificate, ReturnSummary, BusinessType}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingDataSource

import java.time.LocalDate
import scala.concurrent.Future

class GamblingDataCacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class GamblingRdsStub extends GamblingDataSource {

    override def getReturnSummary(mgdRegNumber: String): Future[ReturnSummary] =
      Future.successful(GamblingStubData.getReturnSummary(mgdRegNumber))

    override def getMgdCertificate(mgdRegNumber: String): Future[MgdCertificate] =
      Future.successful(GamblingStubData.getMgdCertificate(mgdRegNumber))

    override def getOperatorDetails(mgdRegNumber: String): Future[OperatorDetails] =
      Future.successful(GamblingStubData.getOperatorDetails(mgdRegNumber))

    override def getBusinessName(mgdRegNumber: String): Future[BusinessName] =
      Future.successful(GamblingStubData.getBusinessName(mgdRegNumber))

    override def getBusinessDetails(mgdRegNumber: String): Future[BusinessDetails] =
      Future.successful(GamblingStubData.getBusinessDetails(mgdRegNumber))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[GamblingDataSource].toInstance(new GamblingRdsStub)
    )
    .build()

  private lazy val repository: GamblingDataSource =
    app.injector.instanceOf[GamblingDataSource]

  "getOperatorDetails (stubbed repository)" should {

    "return operator details for a valid mgdRegNumber" in {
      val result = repository.getOperatorDetails("XYZ00000000001").futureValue

      result.mgdRegNumber mustBe "XYZ00000000001"
      result.businessName must not be empty
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getOperatorDetails("XYZ00000000001").futureValue
      val result2 = repository.getOperatorDetails("XYZ00000000001").futureValue

      result1 mustBe result2
    }

    "handle different mgdRegNumbers independently" in {
      val result1 = repository.getOperatorDetails("XYZ00000000001").futureValue
      val result2 = repository.getOperatorDetails("XYZ00000000002").futureValue

      result1 must not be result2
    }

    "propagate downstream failure" in {
      val exception = intercept[RuntimeException] {
        repository.getOperatorDetails("ERR00000000000").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }

    "handle empty result scenario" in {
      val exception = intercept[RuntimeException] {
        repository.getOperatorDetails("EMPTY000000000").futureValue
      }

      exception.getMessage must include("No data")
    }

    "handle null cursor scenario" in {
      val exception = intercept[RuntimeException] {
        repository.getOperatorDetails("NULL000000000").futureValue
      }

      exception.getMessage must include("Null cursor")
    }
  }

  "getBusinessDetails (stubbed repository)" should {

    "return business details for a valid mgdRegNumber" in {
      val result = repository.getBusinessDetails("XYZ00000000001").futureValue

      result.mgdRegNumber mustBe "XYZ00000000001"
      result.currentlyRegistered must be >= 0
    }

    "populate optional fields correctly" in {
      val result = repository.getBusinessDetails("XYZ00000000001").futureValue

      result.businessType mustBe None
      result.businessPartnerNumber mustBe Some("BPN123456")
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getBusinessDetails("XYZ00000000001").futureValue
      val result2 = repository.getBusinessDetails("XYZ00000000001").futureValue

      result1 mustBe result2
    }

    "handle different mgdRegNumbers independently" in {
      val result1 = repository.getBusinessDetails("XYZ00000000001").futureValue
      val result2 = repository.getBusinessDetails("XYZ00000000002").futureValue

      result1 must not be result2
    }

    "propagate downstream failure" in {
      val exception = intercept[RuntimeException] {
        repository.getBusinessDetails("ERR00000000000").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }

    "handle no data scenario" in {
      val exception = intercept[RuntimeException] {
        repository.getBusinessDetails("EMPTY000000000").futureValue
      }

      exception.getMessage must include("No business details found")
    }

    "handle special characters in mgdRegNumber" in {
      val result = repository.getBusinessDetails("XYZ-123/ABC").futureValue

      result.mgdRegNumber mustBe "XYZ-123/ABC"
    }
  }

  "getReturnSummary (stubbed repository)" should {

    "return zero counts when no returns are due or overdue" in {
      val result = repository.getReturnSummary("XYZ00000000000").futureValue

      result mustBe ReturnSummary(
        mgdRegNumber   = "XYZ00000000000",
        returnsDue     = 0,
        returnsOverdue = 0
      )
    }

    "return overdue count correctly" in {
      val result = repository.getReturnSummary("XYZ00000000001").futureValue

      result.returnsDue mustBe 0
      result.returnsOverdue mustBe 1
    }

    "return due count correctly" in {
      val result = repository.getReturnSummary("XYZ00000000010").futureValue

      result.returnsDue mustBe 1
      result.returnsOverdue mustBe 0
    }

    "return both due and overdue counts correctly" in {
      val result = repository.getReturnSummary("XYZ00000000012").futureValue

      result mustBe ReturnSummary("XYZ00000000012", 1, 2)
    }

    "handle multiple due and overdue values" in {
      val result = repository.getReturnSummary("XYZ00000000021").futureValue

      result.returnsDue mustBe 2
      result.returnsOverdue mustBe 1
    }

    "return default values for unknown mgdRegNumber" in {
      val result = repository.getReturnSummary("XYZ99999999999").futureValue

      result mustBe ReturnSummary("XYZ99999999999", 3, 4)
    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getReturnSummary("XYZ00000000012").futureValue
      val result2 = repository.getReturnSummary("XYZ00000000012").futureValue

      result1 mustBe result2
    }

    "handle different valid mgdRegNumbers independently" in {
      val result1 = repository.getReturnSummary("XYZ00000000010").futureValue
      val result2 = repository.getReturnSummary("XYZ00000000001").futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getReturnSummary("ERR00000000000").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }

    "handle special characters in mgdRegNumber" in {
      val result = repository.getReturnSummary("XYZ-123/ABC").futureValue

      result mustBe ReturnSummary("XYZ-123/ABC", 3, 4)
    }

    "handle whitespace mgdRegNumber" in {
      val result = repository.getReturnSummary("   ").futureValue

      result mustBe ReturnSummary("   ", 3, 4)
    }

    "return populated fields for all responses" in {
      val result = repository.getReturnSummary("XYZ00000000012").futureValue

      result.mgdRegNumber   must not be empty
      result.returnsDue     must be >= 0
      result.returnsOverdue must be >= 0
    }
  }
  "getBusinessName (stubbed repository)" should {

   "return John Doe as Sole Proprietor" in {
     val result = repository.getBusinessName("XYZ00000000000").futureValue

     result mustBe BusinessName(
       mgdRegNumber = "XYZ00000000000",
       solePropTitle = Some("Mr"),
       solePropFirstName = Some("John"),
       solePropMidName = Some("C"),
       solePropLastName = Some("Doe"),
       businessName = Some("John Doe Co."),
       businessType = Some(BusinessType.SoleProprietor),
       tradingName = Some("DoeDoe"),
       systemDate = Some(LocalDate.of(2026, 4, 20))
     )
   }

   "return Marge Simpson as Sole Proprietor" in {
     val result = repository.getBusinessName("XYZ00000000010").futureValue

     result mustBe BusinessName(
       mgdRegNumber = "XYZ00000000010",
       solePropTitle = Some("Mrs"),
       solePropFirstName = Some("Marge"),
       solePropMidName = Some("Jacqueline"),
       solePropLastName = Some("Simpson"),
       businessName = Some("Pretzel Wagon"),
       businessType = Some(BusinessType.SoleProprietor),
       tradingName = Some("Marge Simpson"),
       systemDate = Some(LocalDate.of(2026, 4, 20))
     )
   }

   "return last name and business name correctly" in {
     val result = repository.getBusinessName("XYZ00000000001").futureValue

     result.solePropLastName mustBe Some("Doe")
     result.businessName mustBe Some("Jane Doe Co.")
   }

   "return correct middle name and system date" in {
     val result = repository.getBusinessName("XYZ00000000010").futureValue

     result.solePropMidName mustBe Some("Jacqueline")
     result.systemDate mustBe Some(LocalDate.of(2026, 4, 20))
   }

   "return correct title and trading name" in {
     val result = repository.getBusinessName("XYZ00000000012").futureValue

     result.solePropTitle mustBe Some("Miss")
     result.tradingName mustBe Some("Miss Havisham")
   }

   "return correct business type and first name" in {
     val result = repository.getBusinessName("XYZ00000000021").futureValue

     result.solePropFirstName mustBe Some("Eugine")
     result.businessType mustBe Some(BusinessType.SoleProprietor)
   }

   "return default values for unknown mgdRegNumber" in {
     val result = repository.getBusinessName("XYZ99999999999").futureValue

     result mustBe BusinessName(
       mgdRegNumber = "XYZ99999999999",
       solePropTitle = Some("Mr"),
       solePropFirstName = Some("Foo"),
       solePropMidName = Some("B"),
       solePropLastName = Some("Bar"),
       businessName = Some("FooBar Co."),
       businessType = Some(BusinessType.SoleProprietor),
       tradingName = Some("Foobar"),
       systemDate = Some(LocalDate.of(2026, 4, 20))
     )
   }

   "return consistent results across multiple calls" in {
     val result1 = repository.getBusinessName("XYZ00000000012").futureValue
     val result2 = repository.getBusinessName("XYZ00000000012").futureValue

     result1 mustBe result2
   }

   "handle different valid mgdRegNumbers independently" in {
     val result1 = repository.getBusinessName("XYZ00000000010").futureValue
     val result2 = repository.getBusinessName("XYZ00000000001").futureValue

     result1 must not be result2
   }

   "propagate downstream failure from stub" in {
     val exception = intercept[RuntimeException] {
       repository.getBusinessName("ERR00000000000").futureValue
     }

     exception.getMessage must include("Simulated downstream failure")
   }

   "handle special characters in mgdRegNumber" in {
     val result = repository.getBusinessName("XYZ-123/ABC").futureValue

     result.mgdRegNumber mustBe "XYZ-123/ABC"
   }

   "handle whitespace mgdRegNumber" in {
     val result = repository.getBusinessName("   ").futureValue

     result.mgdRegNumber mustBe ("   ")
   }

   "return populated fields for all required responses" in {
     val result = repository.getBusinessName("XYZ00000000012").futureValue

     result.mgdRegNumber must not be empty
     result.solePropTitle must not be empty
     result.solePropFirstName must not be empty
     result.solePropLastName must not be empty
     result.businessName must not be empty
     result.tradingName must not be empty
   }
 }

  "getBusinessDetails (stubbed repository)" should {

    "return values correctly" in {
      val result = repository.getBusinessDetails("XYZ00000000000").futureValue

      result mustBe BusinessDetails(
        mgdRegNumber = "XYZ00000000000",
        businessType = Some(BusinessType.LimitedLiabilityPartnership),
        currentlyRegistered = 1,
        groupReg = true,
        dateOfRegistration = Some(LocalDate.of(2024, 4, 21)), businessPartnerNumber = Some("bar"), systemDate = LocalDate.of(2024, 4, 21)
      )
    }

    "return business type correctly" in {
      val result = repository.getBusinessDetails("XYZ00000000001").futureValue

      result.businessType mustBe Some(BusinessType.LimitedLiabilityPartnership)
      result.currentlyRegistered mustBe 1
      result.groupReg mustBe true
      result.dateOfRegistration mustBe Some(LocalDate.of(2024, 4, 21))
      result.businessPartnerNumber mustBe Some("bar")
      result.systemDate mustBe LocalDate.of(2024, 4, 21)
    }

    "return group reg correctly" in {
      val result = repository.getBusinessDetails("XYZ00000000010").futureValue

      result.businessType mustBe Some(BusinessType.UnincorporatedBody)
      result.currentlyRegistered mustBe 2
      result.groupReg mustBe true
      result.dateOfRegistration mustBe Some(LocalDate.of(2024, 4, 21))
      result.businessPartnerNumber mustBe Some("bar")
      result.systemDate mustBe LocalDate.of(2024, 4, 21)
    }

    "return both date values correctly" in {
      val result = repository.getBusinessDetails("XYZ00000000012").futureValue

      result mustBe BusinessDetails("XYZ00000000012", Some(BusinessType.UnincorporatedBody), 2, true, Some(LocalDate.of(2023, 4, 21)), Some("barfoo"), LocalDate.of(2023, 4, 21))
    }

    "handle multiple values" in {
      val result = repository.getBusinessDetails("XYZ00000000021").futureValue

      result.businessType mustBe Some(BusinessType.Partnership)
      result.currentlyRegistered mustBe 2
      result.groupReg mustBe false
      result.dateOfRegistration mustBe Some(LocalDate.of(2024, 1, 21))
      result.businessPartnerNumber mustBe Some("barbar")
      result.systemDate mustBe LocalDate.of(2024, 1, 21)
    }



    "handle different valid mgdRegNumbers independently" in {
      val result1 = repository.getBusinessDetails("XYZ00000000010").futureValue
      val result2 = repository.getBusinessDetails("XYZ00000000001").futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getBusinessDetails("ERR00000000000").futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }


    "handle whitespace mgdRegNumber" in {
      val result = repository.getBusinessDetails("   ").futureValue

      result mustBe BusinessDetails("   ", Some(BusinessType.SoleProprietor), 0,  true, Some(LocalDate.of(2026, 4, 22)), Some("unknown"), LocalDate.of(2026, 4, 22))
    }
  }
}
