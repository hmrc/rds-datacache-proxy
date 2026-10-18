package uk.gov.hmrc.rdsdatacacheproxy.pas.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.PasCisSource
import uk.gov.hmrc.rdsdatacacheproxy.pas.stub.PasCisStubData

import scala.concurrent.Future

class PasCisControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class PasCisStub extends PasCisSource {
    override def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse] =
      Future.successful(
        PasCisStubData.getCreateCisAppealResponse(request)
      )
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides{
        bind[PasCisSource].toInstance(new PasCisStub())
      }
      .build()


  private final val endpoint = "/pas"

  "POST /pas/cis/appeal" should {

    "return 201 with a valid CreateCisAppealResponse" in {
      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L
      )
      val response = post(s"$endpoint/cis/appeal", requestJson).futureValue

      response.status mustBe CREATED
      response.contentType mustBe "application/json"

      response.json.as[CreateCisAppealResponse] mustBe PasCisStubData.createCisAppealSuccessResponse
    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val requestJson = Json.obj(
        "penaltyId" -> 500L
      )
      val response = post(s"$endpoint/cis/appeal", requestJson).futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val requestJson = Json.obj(
        "penaltyId" -> 1L
      )
      val response = post(s"$endpoint/cis/appeal", requestJson).futureValue

      response.status mustBe UNAUTHORIZED
    }
  }
}
