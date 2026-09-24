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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.gpa

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.CompanyNominator
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.gpa.CompanyNominatorService

import scala.concurrent.Future

class CompanyNominatorControllerSpec extends SpecBase with MockitoSugar {

  private class Setup {
    val mockService: CompanyNominatorService = mock[CompanyNominatorService]
    val controller: CompanyNominatorController = new CompanyNominatorController(fakeAuthAction, mockService, cc)

    val companyNominatorTrue: CompanyNominator = CompanyNominator(isParticipator = "Y")
    val companyNominatorFalse: CompanyNominator = CompanyNominator(isParticipator = "N")
  }

  "RepaymentsControllerSpec" - {
    "return a 200 and company nominator when isParticipator is 'Y' " in new Setup {
      when(mockService.getIsCompanyNominatorOfGPA(any[Long], any[Long]))
        .thenReturn(Future.successful(companyNominatorTrue))

      val result: Future[Result] = controller.getIsCompanyNominatorOfGPA(6212811176L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getIsCompanyNominatorOfGPA(6212811176L, 2L)
    }

    "return a 200 and company nominator when isParticipator is 'N'" in new Setup {
      when(mockService.getIsCompanyNominatorOfGPA(any[Long], any[Long]))
        .thenReturn(Future.successful(companyNominatorFalse))

      val result: Future[Result] = controller.getIsCompanyNominatorOfGPA(6212811176L, 2L)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getIsCompanyNominatorOfGPA(6212811176L, 2L)
    }

    "return 500 and when repository call fails" in new Setup {
      when(mockService.getIsCompanyNominatorOfGPA(any[Long], any[Long]))
        .thenReturn(Future.failed(new RuntimeException("Error")))

      val result: Future[Result] = controller.getIsCompanyNominatorOfGPA(1L, 10L)(fakeRequest)

      status(result)      shouldBe INTERNAL_SERVER_ERROR
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getIsCompanyNominatorOfGPA(1L, 10L)
    }

  }
}
