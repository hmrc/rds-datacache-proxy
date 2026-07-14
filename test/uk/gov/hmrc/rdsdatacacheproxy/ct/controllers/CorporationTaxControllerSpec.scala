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
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.Json

import scala.concurrent.Future
import play.api.mvc.Result
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{InterestAccural, Penalties, PenaltyTransaction}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.CorporationTaxDatacacheRepositoryImpl

import java.time.LocalDate

class CorporationTaxControllerSpec extends SpecBase with MockitoSugar {

  private class SetUp {
    val mockCorporationTaxDatacacheRepository: CorporationTaxDatacacheRepositoryImpl = mock[CorporationTaxDatacacheRepositoryImpl]
    val controller: CorporationTaxController = new CorporationTaxController(fakeAuthAction, mockCorporationTaxDatacacheRepository, cc)

    val emptyPenaltiesList: List[PenaltyTransaction] = List[PenaltyTransaction]()
    val penaltiesList: List[PenaltyTransaction] =
      List(
        PenaltyTransaction(penaltyDate = LocalDate.of(2025, 5, 1), `type` = "F", postingAmount = BigDecimal(100.13)),
        PenaltyTransaction(penaltyDate = LocalDate.of(2021, 3, 7), `type` = "G", postingAmount = BigDecimal(27.19))
      )

    val emptyInterestAccuralList: List[InterestAccural] = List[InterestAccural]()

  }

  "CorporationTaxController#getPenaltyTransactionList" - {
    "return 200 and a successful response when repository return empty penalties list " in new SetUp {
      when(mockCorporationTaxDatacacheRepository.getPenaltyTransactionList(any[Long], any[Long]))
        .thenReturn(Future.successful(emptyPenaltiesList))

      val result: Future[Result] = controller.getPenaltyTransactionList(1L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockCorporationTaxDatacacheRepository).getPenaltyTransactionList(1L, 2L)
    }

    "return 200 and a successful response when repository return penalties with two items " in new SetUp {
      when(mockCorporationTaxDatacacheRepository.getPenaltyTransactionList(any[Long], any[Long]))
        .thenReturn(Future.successful(penaltiesList))

      val result: Future[Result] = controller.getPenaltyTransactionList(1L, 2L)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(Penalties(penaltyTransactions = penaltiesList))
      verify(mockCorporationTaxDatacacheRepository).getPenaltyTransactionList(1L, 2L)
    }

    "return 500 and when repository call fails" in new SetUp {
      when(mockCorporationTaxDatacacheRepository.getPenaltyTransactionList(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getPenaltyTransactionList(3L, 7L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockCorporationTaxDatacacheRepository).getPenaltyTransactionList(3L, 7L)
    }

  }

  "CorporationTaxController#getInterestAccuralList" - {
    val taxRef: Long = 17L
    val accPeriod: Long = 2L
    val interestType: String = "IDE"

    "return 200 and a successful response when repository return empty interest accural list " in new SetUp {
      when(mockCorporationTaxDatacacheRepository.getInterestAccuralList(any[Long], any[Long], any[String]))
        .thenReturn(Future.successful(emptyInterestAccuralList))

      val result: Future[Result] = controller.getInterestAccuralList(taxRef, accPeriod, interestType)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockCorporationTaxDatacacheRepository).getInterestAccuralList(taxRef, accPeriod, interestType)
    }

  }

}
