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

package uk.gov.hmrc.rdsdatacacheproxy

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.{Configuration, Environment}
import play.api.inject.Binding
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ndds.repositories.RdsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.ndds.controllers.DirectDebitController

class ModuleSpec extends AnyWordSpec with Matchers {

  val environment: Environment = Environment.simple()

  "Module bindings" should {

    "bind AuthAction to DefaultAuthAction" in {
      val config = Configuration("feature-switch.rds-stubbed" -> false)
      val module = new Module()
      val bindings = module.bindings(environment, config)

      bindings.exists(b => b.key.clazz == classOf[AuthAction]) shouldBe true
    }

    "bind DirectDebitController to self" in {
      val config = Configuration("feature-switch.rds-stubbed" -> false)
      val module = new Module()
      val bindings = module.bindings(environment, config)

      bindings.exists(b => b.key.clazz == classOf[DirectDebitController]) shouldBe true
    }
  }
}
