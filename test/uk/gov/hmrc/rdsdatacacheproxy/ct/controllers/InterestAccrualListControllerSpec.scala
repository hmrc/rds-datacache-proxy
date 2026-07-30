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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestAccrual
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.InterestAccrualService

import scala.concurrent.Future

class InterestAccrualListControllerSpec extends SpecBase with MockitoSugar {

  private class SetUp {
    val mockService: InterestAccrualService = mock[InterestAccrualService]
    val controller: InterestAccrualListController = new InterestAccrualListController(fakeAuthAction, mockService, cc)

    val emptyInterestAccrualList: List[InterestAccrual] = List[InterestAccrual]()

  }

  "InterestAccrualListControllerSpec" - {
    val taxRef: Long = 17L
    val accPeriod: Long = 2L
    val interestType: String = "IDE"

    "return 200 and a successful response when repository return empty interest accrual list " in new SetUp {
      when(mockService.getInterestAccrualList(any[Long], any[Long], any[String]))
        .thenReturn(Future.successful(emptyInterestAccrualList))

      val result: Future[Result] = controller.getInterestAccrualList(taxRef, accPeriod, interestType)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getInterestAccrualList(taxRef, accPeriod, interestType)
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
