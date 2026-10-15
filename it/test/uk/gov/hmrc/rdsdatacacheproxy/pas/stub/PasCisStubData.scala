package uk.gov.hmrc.rdsdatacacheproxy.pas.stub

import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse}

object PasCisStubData {
  
  val createCisAppealSuccessResponse = CreateCisAppealResponse(created = true)
  
  def getCreateCisAppealResponse(createCisAppealRequest: CreateCisAppealRequest): CreateCisAppealResponse = {
    createCisAppealRequest.penaltyId match {
      case 1 => createCisAppealSuccessResponse
      case _ => throw new RuntimeException("Error from downstream")
    }
  }
}
