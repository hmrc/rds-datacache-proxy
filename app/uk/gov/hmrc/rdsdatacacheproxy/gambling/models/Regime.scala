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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.models

sealed trait Regime(val code: String)

object Regime {

  case object GBD extends Regime("gbd")
  case object PBD extends Regime("pbd")
  case object RGD extends Regime("rgd")
  case object MGD extends Regime("mgd")

  val values: Seq[Regime] =
    Seq(GBD, PBD, RGD, MGD)

  def fromString(s: String): Option[Regime] =
    values.find(_.code == s.trim.toLowerCase)

  def contains(s: String): Boolean =
    values.exists(_.code == s.trim.toLowerCase)
}
