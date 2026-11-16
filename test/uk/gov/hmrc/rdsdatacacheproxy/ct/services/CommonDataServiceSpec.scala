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

import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CommonData
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.CommonDataRepository

import java.time.LocalDate
import scala.concurrent.Future

class CommonDataServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  private class Setup {
    val mockRepository: CommonDataRepository = mock[CommonDataRepository]
    val service = new CommonDataService(mockRepository)

    val commonData: CommonData = CommonData(
      asOnDate = LocalDate.of(2026, 9, 15)
    )
  }

  "getCommonData returns data retrieved from repository" in new Setup {

    when(mockRepository.getCommonData())
      .thenReturn(Future.successful(commonData))

    val result: CommonData = service.getCommonData().futureValue

    result mustBe commonData
    verify(mockRepository).getCommonData()
  }

  "getCommonData returns failure from repository" in new Setup {

    val ex = new RuntimeException("boom")

    when(mockRepository.getCommonData())
      .thenReturn(Future.failed(ex))

    val result: Throwable = service.getCommonData().failed.futureValue

    result mustBe ex
    verify(mockRepository).getCommonData()
    verifyNoMoreInteractions(mockRepository)
  }

}
