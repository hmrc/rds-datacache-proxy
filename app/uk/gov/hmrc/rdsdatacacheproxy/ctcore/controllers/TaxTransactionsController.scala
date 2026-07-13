package uk.gov.hmrc.rdsdatacacheproxy.ctcore.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.rdsdatacacheproxy.actions.AuthAction
import uk.gov.hmrc.rdsdatacacheproxy.ctcore.models.TaxTransactions
import uk.gov.hmrc.rdsdatacacheproxy.ctcore.repositories.TaxTransactionsDataCacheRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TaxTransactionsController @Inject() (authorise: AuthAction, repository: TaxTransactionsDataCacheRepository, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc)
    with Logging {

  def getTaxTransactions(taxRef: Long, accPeriod: Long): Action[AnyContent] =
    authorise.async { implicit request =>
      repository
        .getTaxTransactions(taxRef, accPeriod)
        .map { taxTransactions =>
          Ok(Json.toJson(TaxTransactions(taxTransactions)))
        }
        .recover { case ex: Exception =>
          logger.error("error while retrieving tax transactions", ex)
          InternalServerError("Failed to retrieve tax transactions")
        }
    }
}
