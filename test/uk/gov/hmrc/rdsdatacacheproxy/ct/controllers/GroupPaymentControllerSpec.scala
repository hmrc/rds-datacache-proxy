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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.GroupPaymentsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.GroupPaymentsRepositoryImpl
import scala.concurrent.Future

class GroupPaymentControllerSpec extends SpecBase with MockitoSugar with GroupPaymentsHelper {

  private class SetUp {
    val mockGroupPaymentsRepositoryImpl: GroupPaymentsRepositoryImpl = mock[GroupPaymentsRepositoryImpl]
    val controller: GroupPaymentsController = new GroupPaymentsController(fakeAuthAction, mockGroupPaymentsRepositoryImpl, cc)
  }

  "PenaltiesController#getPenaltyTransactionList" - {

    "return 200::successful response when repository Group Payment record" in new SetUp {
      when(mockGroupPaymentsRepositoryImpl.getGroupSummary(any[Long], any[Long]))
        .thenReturn(Future.successful(Some(groupPaymentDetails)))

      val result: Future[Result] = controller.getGroupSummary(1L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockGroupPaymentsRepositoryImpl).getGroupSummary(1L, 2L)
    }

    "return 200::successful response when repository Group Payment record with empty refs" in new SetUp {
      when(mockGroupPaymentsRepositoryImpl.getGroupSummary(any[Long], any[Long]))
        .thenReturn(Future.successful(Some(groupPaymentDetails)))

      val result: Future[Result] = controller.getGroupSummary(17L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockGroupPaymentsRepositoryImpl).getGroupSummary(17L, 2L)
    }

    "return 500 and when repository call fails" in new SetUp {
      when(mockGroupPaymentsRepositoryImpl.getGroupSummary(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Upstream error")))

      val result: Future[Result] = controller.getGroupSummary(3L, 7L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("text/plain")
      verify(mockGroupPaymentsRepositoryImpl).getGroupSummary(3L, 7L)
    }

  }

}
