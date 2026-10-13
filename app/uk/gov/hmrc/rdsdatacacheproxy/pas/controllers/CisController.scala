package uk.gov.hmrc.rdsdatacacheproxy.pas.controllers

import com.google.inject.Inject
import play.api.Logging
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.CreateCisAppealRequest
import uk.gov.hmrc.rdsdatacacheproxy.pas.services.CisService

import scala.concurrent.{ExecutionContext, Future}

class CisController @Inject() (
                                authorise: AuthAction,
                                service: CisService,
                                cc: ControllerComponents
                              )(implicit ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def createCisAppeal(): Action[JsValue] =
    authorise.async(parse.json) { implicit request =>
      request.body
        .validate[CreateCisAppealRequest]
        .fold(
          errs =>
            Future.successful(BadRequest(Json.obj("message" -> "Invalid payload", "errors" -> JsError.toJson(errs)))),
          body =>
            service
              .createCisAppeal(body)
              .map { CreateVendorReturn =>
                Created(Json.toJson(CreateVendorReturn))
              }
              .recover { case t =>
                logger.error("[createVendor] failed", t)
                InternalServerError(Json.obj("message" -> "Unexpected error"))
              }
        )
    }
}
