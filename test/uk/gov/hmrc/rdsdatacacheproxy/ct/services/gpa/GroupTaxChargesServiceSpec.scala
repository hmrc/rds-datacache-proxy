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

package uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.GroupTaxChargesStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.GpaGroupTaxCharges
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.GroupTaxChargesRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GroupTaxChargesServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with GroupTaxChargesStubData {

  "getGroupTaxCharges must delegate to repository and return GpaGroupTaxCharges" in new Setup {
    when(mockRepo.getGPAGroupTaxCharges(any(), any(), any(), any())).thenReturn(Future.successful(gpaWithNonEmptyParticipator))

    val result: GpaGroupTaxCharges = service.getGpaGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount).futureValue

    result mustBe gpaWithNonEmptyParticipator

    verify(mockRepo, times(1)).getGPAGroupTaxCharges(any(), any(), any(), any())
  }
  "getGroupTaxCharges must delegate to repository and return GpaGroupTaxCharges by trimming spaces from pGppApportionmentMethod" in new Setup {
    when(mockRepo.getGPAGroupTaxCharges(any(), any(), any(), any()))
      .thenReturn(Future.successful(gpaWithEmptyGppApportionmentMethod))

    val result: GpaGroupTaxCharges = service.getGpaGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount).futureValue

    result mustBe gpaWithFilteredGppApportionmentMethod

    verify(mockRepo, times(1)).getGPAGroupTaxCharges(any(), any(), any(), any())
  }

  "getGroupTaxCharges must propagate failure from repository " in new Setup {
    val exception = new RuntimeException("Boom")
    when(mockRepo.getGPAGroupTaxCharges(any(), any(), any(), any())).thenReturn(Future.failed(exception))
    val result: Throwable = service.getGpaGroupTaxCharges(pGpaUtr, pGppContractVersion, pStartIndex, pCount).failed.futureValue

    result mustBe exception

    result.getMessage must include("Boom")

    verify(mockRepo, times(1)).getGPAGroupTaxCharges(any(), any(), any(), any())

    verifyNoMoreInteractions(mockRepo)

  }

  trait Setup {
    val mockRepo: GroupTaxChargesRepository = mock[GroupTaxChargesRepository]
    val service = new GroupTaxChargesService(mockRepo)
    val pGpaUtr = 19L
    val pGppContractVersion = 12L
    val pStartIndex = 21L
    val pCount = 33L
  }
}
