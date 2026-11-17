package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.mockito.Mockito.{verify, when}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, contentType, status}
import uk.gov.hmrc.rdsdatacacheproxy.base.SpecBase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CommonData
import uk.gov.hmrc.rdsdatacacheproxy.ct.services.CommonDataService

import java.time.LocalDate
import scala.concurrent.Future

class CommonDataControllerSpec extends SpecBase {

  private trait Setup {
    val mockService: CommonDataService = mock[CommonDataService]

    val controller = new CommonDataController(fakeAuthAction, mockService, cc)

    val commonData: CommonData = CommonData(
      asOnDate = LocalDate.of(2026, 9, 15)
    )
  }

  "getCommonData" - {

    "should return 200 OK with common data" in new Setup {

      when(mockService.getCommonData())
        .thenReturn(Future.successful(commonData))

      val result: Future[Result] =
        controller.getCommonData().apply(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(commonData)

      verify(mockService).getCommonData()
    }

    "should return 500 when there is an exception returned from the service" in new Setup {

      when(mockService.getCommonData())
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val result: Future[Result] =
        controller.getCommonData().apply(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "message").as[String] mustBe "Unexpected error"

      verify(mockService).getCommonData()
    }
  }
}
