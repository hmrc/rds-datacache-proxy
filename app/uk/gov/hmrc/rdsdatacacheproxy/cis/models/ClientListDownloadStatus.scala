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

package uk.gov.hmrc.rdsdatacacheproxy.cis.models

import uk.gov.hmrc.rdsdatacacheproxy.shared.models.WithName

sealed trait ClientListDownloadStatus

object ClientListDownloadStatus {
  case object InitiateDownload extends ClientListDownloadStatus with WithName("InitiateDownload")
  case object InProgress       extends ClientListDownloadStatus with WithName("InProgress")
  case object Succeeded        extends ClientListDownloadStatus with WithName("Succeeded")
  case object Failed           extends ClientListDownloadStatus with WithName("Failed")

  def fromInt(status: Int): Either[String, ClientListDownloadStatus] = status match {
    case -1 => Right(InitiateDownload)
    case 0  => Right(InProgress)
    case 1  => Right(Succeeded)
    case 2  => Right(Failed)
    case _  => Left("Could not map client list download status")
  }
}
