package uk.gov.hmrc.rdsdatacacheproxy.pas.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse}
import uk.gov.hmrc.rdsdatacacheproxy.pas.repositories.CisRepository

import javax.inject.Inject
import scala.concurrent.Future

class CisService @Inject (repository: CisRepository) {

  def createCisAppeal(createCisAppealRequest: CreateCisAppealRequest)
                     (implicit hc: HeaderCarrier): Future[CreateCisAppealResponse] =
    repository.createCisAppeal(createCisAppealRequest)
}
