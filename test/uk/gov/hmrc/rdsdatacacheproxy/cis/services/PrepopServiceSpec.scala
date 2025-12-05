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

package uk.gov.hmrc.rdsdatacacheproxy.cis.services

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.cis.models.{SchemePrepop, SubcontractorPrepopRecord}
import uk.gov.hmrc.rdsdatacacheproxy.cis.repositories.CisMonthlyReturnSource

import scala.concurrent.Future

final class PrepopServiceSpec extends SpecBase {

  private val source = mock[CisMonthlyReturnSource]
  private val service = new PrepopService(source)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(source)
  }

  private val taxOfficeNumber = "123"
  private val taxOfficeReference = "AB456"
  private val agentOwnReference = "123PA12345678"

  "PrepopService.getSchemePrepopByKnownFacts" - {

    "return SchemePrepop when the repository returns Some(SchemePrepop)" in {
      val scheme = SchemePrepop(
        taxOfficeNumber    = taxOfficeNumber,
        taxOfficeReference = taxOfficeReference,
        agentOwnReference  = agentOwnReference,
        utr                = Some("1123456789"),
        schemeName         = "Test Scheme"
      )

      when(
        source.getSchemePrepopByKnownFacts(
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeReference),
          eqTo(agentOwnReference)
        )
      ).thenReturn(Future.successful(Some(scheme)))

      val out =
        service
          .getSchemePrepopByKnownFacts(taxOfficeNumber, taxOfficeReference, agentOwnReference)
          .futureValue

      out mustBe scheme

      verify(source).getSchemePrepopByKnownFacts(
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference),
        eqTo(agentOwnReference)
      )
      verifyNoMoreInteractions(source)
    }

    "fail with NoSuchElementException (including TON/TOR/AO) when the repository returns None" in {
      when(
        source.getSchemePrepopByKnownFacts(
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeReference),
          eqTo(agentOwnReference)
        )
      ).thenReturn(Future.successful(None))

      val ex =
        service
          .getSchemePrepopByKnownFacts(taxOfficeNumber, taxOfficeReference, agentOwnReference)
          .failed
          .futureValue

      ex mustBe a[NoSuchElementException]
      ex.getMessage must include("TON=123")
      ex.getMessage must include("TOR=AB456")
      ex.getMessage must include("AO=123PA12345678")

      verify(source).getSchemePrepopByKnownFacts(
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference),
        eqTo(agentOwnReference)
      )
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("db exploded", 502)

      when(
        source.getSchemePrepopByKnownFacts(
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeReference),
          eqTo(agentOwnReference)
        )
      ).thenReturn(Future.failed(boom))

      val ex =
        service
          .getSchemePrepopByKnownFacts(taxOfficeNumber, taxOfficeReference, agentOwnReference)
          .failed
          .futureValue

      ex mustBe boom

      verify(source).getSchemePrepopByKnownFacts(
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference),
        eqTo(agentOwnReference)
      )
      verifyNoMoreInteractions(source)
    }
  }

  "PrepopService.getSubcontractorPrepopByKnownFacts" - {

    "return a non-empty Seq[SubcontractorPrepopRecord] when the repository returns data" in {
      val sub = SubcontractorPrepopRecord(
        subcontractorType  = "I",
        subcontractorUtr   = "1123456789",
        verificationNumber = "12345678901",
        verificationSuffix = Some("AB"),
        title              = Some("Mr"),
        firstName          = Some("Bob"),
        secondName         = None,
        surname            = Some("Builder"),
        tradingName        = Some("Bob Builder Ltd")
      )

      when(
        source.getSubcontractorPrepopByKnownFacts(
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeReference),
          eqTo(agentOwnReference)
        )
      ).thenReturn(Future.successful(Seq(sub)))

      val out =
        service
          .getSubcontractorPrepopByKnownFacts(taxOfficeNumber, taxOfficeReference, agentOwnReference)
          .futureValue

      out mustBe Seq(sub)

      verify(source).getSubcontractorPrepopByKnownFacts(
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference),
        eqTo(agentOwnReference)
      )
      verifyNoMoreInteractions(source)
    }

    "fail with NoSuchElementException (including TON/TOR/AO) when the repository returns an empty Seq" in {
      when(
        source.getSubcontractorPrepopByKnownFacts(
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeReference),
          eqTo(agentOwnReference)
        )
      ).thenReturn(Future.successful(Seq.empty))

      val ex =
        service
          .getSubcontractorPrepopByKnownFacts(taxOfficeNumber, taxOfficeReference, agentOwnReference)
          .failed
          .futureValue

      ex mustBe a[NoSuchElementException]
      ex.getMessage must include("TON=123")
      ex.getMessage must include("TOR=AB456")
      ex.getMessage must include("AO=123PA12345678")

      verify(source).getSubcontractorPrepopByKnownFacts(
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference),
        eqTo(agentOwnReference)
      )
      verifyNoMoreInteractions(source)
    }

    "propagate upstream failures from the repository" in {
      val boom = UpstreamErrorResponse("db exploded", 502)

      when(
        source.getSubcontractorPrepopByKnownFacts(
          eqTo(taxOfficeNumber),
          eqTo(taxOfficeReference),
          eqTo(agentOwnReference)
        )
      ).thenReturn(Future.failed(boom))

      val ex =
        service
          .getSubcontractorPrepopByKnownFacts(taxOfficeNumber, taxOfficeReference, agentOwnReference)
          .failed
          .futureValue

      ex mustBe boom

      verify(source).getSubcontractorPrepopByKnownFacts(
        eqTo(taxOfficeNumber),
        eqTo(taxOfficeReference),
        eqTo(agentOwnReference)
      )
      verifyNoMoreInteractions(source)
    }
  }
}
