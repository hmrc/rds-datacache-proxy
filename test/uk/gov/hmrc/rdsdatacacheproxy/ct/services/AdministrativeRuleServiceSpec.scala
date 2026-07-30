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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.AdminRuleHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdminRule
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AdministrativeRuleRepository

import scala.concurrent.Future

class AdministrativeRuleServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with AdminRuleHelper {

  "getAdminRule must delegate to repository and return AdminRule" in new Setup {
    when(mockRepo.getAdminRule(any())).thenReturn(Future.successful(adminRuleWithAllFields))

    val result: AdminRule = service.getAdminRule(adminRuleKey).futureValue

    result mustBe adminRuleWithAllFields

    verify(mockRepo, times(1)).getAdminRule(any())
  }

  "getAdminRule must propagate failure from repository " in new Setup {
    val exception = new RuntimeException("Boom")

    when(mockRepo.getAdminRule(any())).thenReturn(Future.failed(exception))

    val result: Throwable = service.getAdminRule(adminRuleKey).failed.futureValue

    result mustBe exception

    result.getMessage must include("Boom")

    verify(mockRepo, times(1)).getAdminRule(any())

    verifyNoMoreInteractions(mockRepo)

  }

  trait Setup {
    val mockRepo: AdministrativeRuleRepository = mock[AdministrativeRuleRepository]
    val service = new AdministrativeRuleService(mockRepo)
    val adminRuleKey: String = "INST-PERIOD"
  }
}
