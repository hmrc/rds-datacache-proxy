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
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.CompanyNominator
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.CompanyNominatorRepository

import scala.concurrent.Future

class CompanyNominatorServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar {

  private trait Setup {

    val mockRepo: CompanyNominatorRepository = mock[CompanyNominatorRepository]

    val service = new CompanyNominatorService(mockRepo)

    val companyNominatorTrue: CompanyNominator = CompanyNominator(isParticipator = "Y")
    val companyNominatorFalse: CompanyNominator = CompanyNominator(isParticipator = "N")
  }

  "CompanyNominatorServiceSpec" - {
    "must return company nominator when isParticipator is 'Y'" in new Setup {
      when(mockRepo.getIsCompanyNominatorOfGPA(any[Long], any[Long]))
        .thenReturn(Future.successful(companyNominatorTrue))

      val result = service.getIsCompanyNominatorOfGPA(6212811176L, 2L).futureValue

      result mustBe companyNominatorTrue

      verify(mockRepo, times(1)).getIsCompanyNominatorOfGPA(6212811176L, 2L)
    }

    "must return company nominator when isParticipator is 'N'" in new Setup {
      when(mockRepo.getIsCompanyNominatorOfGPA(any[Long], any[Long]))
        .thenReturn(Future.successful(companyNominatorFalse))

      val result = service.getIsCompanyNominatorOfGPA(6212811176L, 2L).futureValue

      result mustBe companyNominatorFalse

      verify(mockRepo, times(1)).getIsCompanyNominatorOfGPA(6212811176L, 2L)

    }

    "must propagate failure from repository" in new Setup {
      val exception = new RuntimeException("Error")

      when(mockRepo.getIsCompanyNominatorOfGPA(any[Long], any[Long]))
        .thenReturn(Future.failed(exception))

      val result = service.getIsCompanyNominatorOfGPA(1L, 2L).failed.futureValue

      result mustBe exception

      verify(mockRepo, times(1)).getIsCompanyNominatorOfGPA(1L, 2L)
    }
  }

}
