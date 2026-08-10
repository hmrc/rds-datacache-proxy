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
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{ReallocationRow, Reallocations}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.ReallocationDatacacheRepositoryImpl

import java.time.LocalDate
import scala.concurrent.Future

class ReallocationControllerSpec extends SpecBase with MockitoSugar {

  trait Fixture {
    val mockReallocationDatacacheRepository: ReallocationDatacacheRepositoryImpl = mock[ReallocationDatacacheRepositoryImpl]
    val controller: ReallocationController = new ReallocationController(fakeAuthAction, cc, mockReallocationDatacacheRepository)

    // Reallocations
    val reallocationsEmpty: Seq[ReallocationRow] = Seq[ReallocationRow]()
    val reallocationsSingleItem: Seq[ReallocationRow] = Seq[ReallocationRow](
      ReallocationRow(
        amount                  = BigDecimal(117.01),
        reallocationDate        = LocalDate.of(2025, 5, 1),
        sourceApEndDate         = LocalDate.of(2026, 7, 1),
        sourceTaxpayerReference = "9369369363"
      )
    )

    val reallocationsTwoItems: Seq[ReallocationRow] = Seq[ReallocationRow](
      ReallocationRow(
        amount                  = BigDecimal(117.01),
        reallocationDate        = LocalDate.of(2025, 5, 1),
        sourceApEndDate         = LocalDate.of(2026, 7, 1),
        sourceTaxpayerReference = "9369369363"
      ),
      ReallocationRow(
        amount                  = BigDecimal(27.89),
        reallocationDate        = LocalDate.of(2015, 1, 1),
        sourceApEndDate         = LocalDate.of(2025, 11, 8),
        sourceTaxpayerReference = "9369369361"
      )
    )
  }

  "ReallocationController#getByAccountingPeriod" - {
    "return 200 and a successful response when repository return empty reallocations list " in new Fixture {
      when(mockReallocationDatacacheRepository.getByAccountingPeriod(any[Long], any[Long]))
        .thenReturn(Future.successful(reallocationsTwoItems))

      val result: Future[Result] = controller.getByAccountingPeriod(1L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockReallocationDatacacheRepository).getByAccountingPeriod(1L, 2L)
    }

    "return 200 and a successful response when repository returns two items" in new Fixture {
      when(mockReallocationDatacacheRepository.getByAccountingPeriod(any[Long], any[Long]))
        .thenReturn(Future.successful(reallocationsTwoItems))

      val result: Future[Result] = controller.getByAccountingPeriod(2L, 3L)(fakeRequest)

      status(result)        shouldBe OK
      contentType(result)   shouldBe Some("application/json")
      contentAsJson(result) shouldBe Json.toJson(Reallocations(reallocationsTwoItems))
      verify(mockReallocationDatacacheRepository).getByAccountingPeriod(2L, 3L)
    }

    "return 500 and when repository call fails" in new Fixture {
      when(mockReallocationDatacacheRepository.getByAccountingPeriod(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Database error")))

      val result: Future[Result] = controller.getByAccountingPeriod(3L, 7L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockReallocationDatacacheRepository).getByAccountingPeriod(3L, 7L)
    }
  }

}
