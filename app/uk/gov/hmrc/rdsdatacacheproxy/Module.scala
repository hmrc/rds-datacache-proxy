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

import play.api.inject.{Binding, Module as AppModule}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.rdsdatacacheproxy.actions.{AuthAction, DefaultAuthAction}
import uk.gov.hmrc.rdsdatacacheproxy.connectors.{RdsDataSource, RdsDatacacheRepository, RdsStub}
import uk.gov.hmrc.rdsdatacacheproxy.controllers.DirectDebitController

class Module extends AppModule:

  override def bindings(
    environment  : Environment,
    configuration: Configuration
  ): Seq[Binding[_]] =
    lazy val rdsStubbed = configuration.get[Boolean]("feature-switch.rds-stubbed")
    lazy val datasource = if (rdsStubbed) classOf[RdsStub] else classOf[RdsDatacacheRepository]

    List(
      bind[AuthAction].to(classOf[DefaultAuthAction]),
      bind[DirectDebitController].toSelf,
      bind[RdsDataSource].to(datasource)
    )
