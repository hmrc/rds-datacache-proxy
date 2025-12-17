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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ClientListDownloadStatusSpec extends AnyWordSpec with Matchers {

  "ClientListDownloadStatus.fromInt" should {

    "return Right(InitiateDownload) when status is -1" in {
      val result = ClientListDownloadStatus.fromInt(-1)
      result mustBe Right(ClientListDownloadStatus.InitiateDownload)
    }

    "return Right(InProgress) when status is 0" in {
      val result = ClientListDownloadStatus.fromInt(0)
      result mustBe Right(ClientListDownloadStatus.InProgress)
    }

    "return Right(Succeeded) when status is 1" in {
      val result = ClientListDownloadStatus.fromInt(1)
      result mustBe Right(ClientListDownloadStatus.Succeeded)
    }

    "return Right(Failed) when status is 2" in {
      val result = ClientListDownloadStatus.fromInt(2)
      result mustBe Right(ClientListDownloadStatus.Failed)
    }

    "return Left with error message when status is not recognized" in {
      val result = ClientListDownloadStatus.fromInt(99)
      result mustBe Left("Could not map client list download status")
    }

    "return Left with error message for negative values other than -1" in {
      val result = ClientListDownloadStatus.fromInt(-2)
      result mustBe Left("Could not map client list download status")
    }
  }

  "ClientListDownloadStatus toString" should {

    "return 'InitiateDownload' for InitiateDownload" in {
      ClientListDownloadStatus.InitiateDownload.toString mustBe "InitiateDownload"
    }

    "return 'InProgress' for InProgress" in {
      ClientListDownloadStatus.InProgress.toString mustBe "InProgress"
    }

    "return 'Succeeded' for Succeeded" in {
      ClientListDownloadStatus.Succeeded.toString mustBe "Succeeded"
    }

    "return 'Failed' for Failed" in {
      ClientListDownloadStatus.Failed.toString mustBe "Failed"
    }
  }
}
