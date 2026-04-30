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
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{AmountDeclared, ReturnsSubmitted}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.GamblingReturnsDataSource

import java.time.LocalDate
import scala.concurrent.Future

class GamblingReturnsDataCacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class GamblingReturnsRdsStub extends GamblingReturnsDataSource {
    override def getReturnsSubmitted(regNumber: String, paginationStart: Int, paginationMaxRows: Int): Future[ReturnsSubmitted] =
      Future.successful(GamblingReturnsStubData.getReturnsSubmitted(regNumber, paginationStart, paginationMaxRows))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GamblingReturnsDataSource].toInstance(new GamblingReturnsRdsStub))
    .build()

  private lazy val repository: GamblingReturnsDataSource = app.injector.instanceOf[GamblingReturnsDataSource]

  "getReturnsSubmitted (stubbed repository)" should {

    "return zero counts when paginationStart is 1" in {
      val result = repository.getReturnsSubmitted("XYZ00000000000", 1, 10).futureValue

      result mustBe ReturnsSubmitted(
        periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
        periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
        total              = Some(0.00),
        totalPeriodRecords = Some(0),
        amountDeclared     = Seq()
      )
    }

    "return correct data when paginationStart is 1" in {
      val result = repository.getReturnsSubmitted("XYZ00000000001", 1, 10).futureValue
      result mustBe ReturnsSubmitted(
        periodStartDate    = Some(LocalDate.of(2013, 3, 1)),
        periodEndDate      = Some(LocalDate.of(2014, 3, 11)),
        total              = Some(-24500.00),
        totalPeriodRecords = Some(3),
        amountDeclared = Seq(
          AmountDeclared(descriptionCode = Some(2650),
                         periodStartDate = Some(LocalDate.of(2014, 4, 1)),
                         periodEndDate   = Some(LocalDate.of(2014, 6, 30)),
                         amount          = Some(-9500.00)
                        ),
          AmountDeclared(descriptionCode = Some(2650),
                         periodStartDate = Some(LocalDate.of(2014, 1, 1)),
                         periodEndDate   = Some(LocalDate.of(2014, 3, 31)),
                         amount          = Some(-8000.00)
                        ),
          AmountDeclared(descriptionCode = Some(2650),
                         periodStartDate = Some(LocalDate.of(2013, 10, 1)),
                         periodEndDate   = Some(LocalDate.of(2013, 12, 31)),
                         amount          = Some(-7000.00)
                        )
            )
        )
    }
//
//    "return due count correctly" in {
//      val result = repository.getReturnsSubmitted("XYZ00000000010",1,10).futureValue
//
//      result.returnsDue mustBe 1
//      result.returnsOverdue mustBe 0
//    }
//
//    "return both due and overdue counts correctly" in {
//      val result = repository.getReturnsSubmitted("XYZ00000000012",1,10).futureValue
//
//      result mustBe ReturnsSubmitted("XYZ00000000012", 1, 2)
//    }
//
//    "handle multiple due and overdue values" in {
//      val result = repository.getReturnsSubmitted("XYZ00000000021",1,10).futureValue
//
//      result.returnsDue mustBe 2
//      result.returnsOverdue mustBe 1
//    }
//
//    "return default values for unknown regNumber" in {
//      val result = repository.getReturnsSubmitted("XYZ99999999999",1,10).futureValue
//
//      result mustBe ReturnsSubmitted("XYZ99999999999", 3, 4)
//    }

    "return consistent results across multiple calls" in {
      val result1 = repository.getReturnsSubmitted("XYZ00000000012", 1, 10).futureValue
      val result2 = repository.getReturnsSubmitted("XYZ00000000012", 1, 10).futureValue

      result1 mustBe result2
    }

    "handle different valid regNumbers independently" in {
      val result1 = repository.getReturnsSubmitted("XYZ00000000010", 1, 10).futureValue
      val result2 = repository.getReturnsSubmitted("XYZ00000000001", 1, 10).futureValue

      result1 must not be result2
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getReturnsSubmitted("ERR00000000000", 1, 10).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }

//    "handle special characters in regNumber" in {
//      val result = repository.getReturnsSubmitted("XYZ-123/ABC",1,10).futureValue
//
//      result mustBe ReturnsSubmitted("XYZ-123/ABC", 3, 4)
//    }
//
//    "handle whitespace regNumber" in {
//      val result = repository.getReturnsSubmitted("   ",1,10).futureValue
//
//      result mustBe ReturnsSubmitted("   ", 3, 4)
//    }
//
//    "return populated fields for all responses" in {
//      val result = repository.getReturnsSubmitted("XYZ00000000012",1,10).futureValue
//
//      result.regNumber must not be empty
//      result.returnsDue must be >= 0
//      result.returnsOverdue must be >= 0
//    }
  }
}
