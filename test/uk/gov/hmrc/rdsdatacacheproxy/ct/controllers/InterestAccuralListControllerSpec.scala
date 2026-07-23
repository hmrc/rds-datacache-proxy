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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.InterestAccural
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.InterestAccuralService

import scala.concurrent.Future

class InterestAccuralListControllerSpec extends SpecBase with MockitoSugar {

  private class SetUp {
    val mockService: InterestAccuralService = mock[InterestAccuralService]
    val controller: InterestAccuralListController = new InterestAccuralListController(fakeAuthAction, mockService, cc)

    val emptyInterestAccuralList: List[InterestAccural] = List[InterestAccural]()

  }

  "CorporationTaxController#getInterestAccuralList" - {
    val taxRef: Long = 17L
    val accPeriod: Long = 2L
    val interestType: String = "IDE"

    "return 200 and a successful response when repository return empty interest accural list " in new SetUp {
      when(mockService.getInterestAccuralList(any[Long], any[Long], any[String]))
        .thenReturn(Future.successful(emptyInterestAccuralList))

      val result: Future[Result] = controller.getInterestAccuralList(taxRef, accPeriod, interestType)(fakeRequest)

      status(result)      shouldBe OK
      contentType(result) shouldBe Some("application/json")
      verify(mockService).getInterestAccuralList(taxRef, accPeriod, interestType)
    }

  }

}
