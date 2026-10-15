/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.pas.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.PasCisSource

import scala.concurrent.Future

final class PasCisServiceSpec extends SpecBase {

  private val source = mock[PasCisSource]
  private val service = new PasCisService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  "PasCisService.createCisAppeal" - {

    "return CreateCisAppealResponse when the repository returns CreateCisAppealResponse" in {
      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val createCisAppealResponse = CreateCisAppealResponse(true)

      when(source.createCisAppeal(createCisAppealRequest)).thenReturn(Future.successful(createCisAppealResponse))

      val out = service.createCisAppeal(createCisAppealRequest).futureValue

      out mustBe createCisAppealResponse

      verify(source).createCisAppeal(eqTo(createCisAppealRequest))
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val boom = UpstreamErrorResponse("database connection failed", 500)

      when(source.createCisAppeal(createCisAppealRequest)).thenReturn(Future.failed(boom))

      val ex = service
        .createCisAppeal(createCisAppealRequest)
        .failed
        .futureValue

      ex mustBe boom
      verify(source).createCisAppeal(createCisAppealRequest)
      verifyNoMoreInteractions(source)
    }
  }
}
