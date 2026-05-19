package uk.gov.hmrc.rdsdatacacheproxy.gambling.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.gambling.services.AssessmentsInAbsenceService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AssessmentsInAbsenceController @Inject() (authorise: AuthAction, service: AssessmentsInAbsenceService, cc: ControllerComponents)(implicit
                                                                                                                     ec: ExecutionContext
) extends BackendController(cc)
  with BaseController
  with Logging {

  def getAssessmentsWithoutReturn(regime: String, regNumber: String, paginationStart: Int, paginationMaxRows: Int): Action[AnyContent] =
    authorise.async { implicit request =>
      service.getAssessmentsWithoutReturn(regime, regNumber, paginationStart, paginationMaxRows).map {
        case Right(assessments) => Ok(Json.toJson(assessments))
        case Left(error)        => handleError(error)
      }
    }
}
