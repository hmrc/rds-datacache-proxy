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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestAccrual
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.InterestAccrualService

import java.time.LocalDate
import scala.concurrent.Future

class InterestAccrualListControllerSpec extends SpecBase with MockitoSugar {

  private class SetUp {
    val mockService: InterestAccrualService = mock[InterestAccrualService]
    val controller: InterestAccrualListController = new InterestAccrualListController(fakeAuthAction, mockService, cc)

    val emptyInterestAccrualList: List[InterestAccrual] = List[InterestAccrual]()

    val interestAccrualListSingleItem: List[InterestAccrual] = List(
      InterestAccrual(
        computationAmount       = 1,
        interestAccrualFromDate = LocalDate.of(2021, 3, 7),
        interestAccrualToDate   = LocalDate.of(2021, 5, 7),
        interestRate            = 2,
        interestAmount          = 10,
        apEndDate               = LocalDate.of(2021, 6, 7)
      )
    )

    val interestAccrualListMultipleItems: List[InterestAccrual] = List(
      InterestAccrual(
        computationAmount       = 1,
        interestAccrualFromDate = LocalDate.of(2021, 3, 7),
        interestAccrualToDate   = LocalDate.of(2021, 5, 7),
        interestRate            = 2,
        interestAmount          = 10,
        apEndDate               = LocalDate.of(2021, 6, 7)
      ),
      InterestAccrual(
        computationAmount       = 1,
        interestAccrualFromDate = LocalDate.of(2021, 6, 10),
        interestAccrualToDate   = LocalDate.of(2021, 8, 10),
        interestRate            = 3,
        interestAmount          = 23,
        apEndDate               = LocalDate.of(2021, 9, 10)
      )
    )

  }

  "InterestAccrualListControllerSpec" - {

    "return 200 and a successful response when repository return a single item" in new SetUp {
      when(mockService.getInterestAccrualList(any[Long], any[Long], any[String]))
        .thenReturn(Future.successful(interestAccrualListSingleItem))

      val result: Future[Result] = controller.getInterestAccrualList(1L, 1L, "IDB")(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getInterestAccrualList(1L, 1L, "IDB")
    }

    "return 200 and a successful response when repository return a multiple items" in new SetUp {
      when(mockService.getInterestAccrualList(any[Long], any[Long], any[String]))
        .thenReturn(Future.successful(interestAccrualListMultipleItems))

      val result: Future[Result] = controller.getInterestAccrualList(2L, 1L, "IDB")(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getInterestAccrualList(2L, 1L, "IDB")
    }

    "return 200 and a successful response when repository return empty interest accrual list " in new SetUp {
      when(mockService.getInterestAccrualList(any[Long], any[Long], any[String]))
        .thenReturn(Future.successful(emptyInterestAccrualList))

      val result: Future[Result] = controller.getInterestAccrualList(17L, 1L, "IDB")(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getInterestAccrualList(17L, 1L, "IDB")
    }

    "return 500 and when repository call fails" in new SetUp {
      when(mockService.getInterestAccrualList(any[Long], any[Long], any[String]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getInterestAccrualList(99L, 1L, "IDB")(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getInterestAccrualList(99L, 1L, "IDB")
    }

  }

}
