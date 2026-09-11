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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.FormDataStub
import java.time.LocalDate

class FormDataRepositoryISpec extends AnyWordSpec
  with Matchers with ScalaFutures with IntegrationPatience
  with GuiceOneAppPerSuite
  with FormDataStub {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FormDataRepository].toInstance(new FormDataRdsStub))
    .build()

  private lazy val repository: FormDataRepository = app.injector.instanceOf[FormDataRepository]

  "getData" should {

    "return default FormDateItem" in {
      val result = repository.getData(1L, 1L,
        LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31") ).futureValue

      result mustBe defaultDataItem
    }

    "return empty FormDateItem" in {
      val result = repository.getData(2L, 1L,
        LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31") ).futureValue

      result mustBe emptyDataItem
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[Error] {
        repository.getData(999L, 1L,
          LocalDate.parse("2006-01-01"), LocalDate.parse("2006-12-31")).futureValue
      }

      exception.getMessage must include("Upstream error")
    }

  }

}
