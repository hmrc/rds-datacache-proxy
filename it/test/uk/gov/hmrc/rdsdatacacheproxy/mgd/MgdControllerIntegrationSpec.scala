package uk.gov.hmrc.rdsdatacacheproxy.mgd

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class MgdControllerIntegrationSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  private val endpoint = "/mgd/return-summary"

  "GET /mgd/return-summary (stubbed repo, no DB)" should {

    "return 200 with correct summary (0,0)" in {
      AuthStub.authorised()
      val res = get(s"$endpoint/XYZ00000000000").futureValue
      res.header("Content-Type") mustBe Some("application/json")

      res.status mustBe OK
      (res.json \ "mgdRegNumber").as[String]   mustBe "XYZ00000000000"
      (res.json \ "returnsDue").as[Int]        mustBe 0
      (res.json \ "returnsOverdue").as[Int]    mustBe 0
    }

    "return 200 with correct summary (1,2)" in {
      AuthStub.authorised()

      val res = get(s"$endpoint/XYZ00000000012").futureValue

      res.status mustBe OK
      (res.json \ "mgdRegNumber").as[String]   mustBe "XYZ00000000012"
      (res.json \ "returnsDue").as[Int]        mustBe 1
      (res.json \ "returnsOverdue").as[Int]    mustBe 2
    }

    "return default stub values for unknown mgdRegNumber" in {
      AuthStub.authorised()

      val res = get(s"$endpoint/XYZ99999999999").futureValue

      res.status mustBe OK
      (res.json \ "mgdRegNumber").as[String]   mustBe "XYZ99999999999"
      (res.json \ "returnsDue").as[Int]        mustBe 3
      (res.json \ "returnsOverdue").as[Int]    mustBe 4
    }

    "normalise lowercase input" in {
      AuthStub.authorised()

      val res = get(s"$endpoint/xyz00000000012 ").futureValue

      res.status mustBe OK
      (res.json \ "mgdRegNumber").as[String] mustBe "XYZ00000000012"
    }

    "return 400 for invalid mgdRegNumber format" in {
      AuthStub.authorised()

      val res = get(s"$endpoint/INVALID").futureValue

      res.status mustBe BAD_REQUEST
      (res.json \ "code").as[String] mustBe "INVALID_MGD_REG_NUMBER"
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val res = get(s"$endpoint/XYZ00000000000").futureValue

      res.status mustBe UNAUTHORIZED
    }

    "return 404 for unknown endpoint (routing sanity)" in {
      AuthStub.authorised()

      val res = get("/does-not-exist").futureValue

      res.status mustBe NOT_FOUND
    }

    "return 500 when stub simulates downstream failure" in {
      AuthStub.authorised()

      val res = get(s"$endpoint/ERR00000000000").futureValue

      res.status mustBe INTERNAL_SERVER_ERROR
      (res.json \ "code").as[String] mustBe "UNEXPECTED_ERROR"
    }


  }
}
