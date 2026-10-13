package uk.gov.hmrc.rdsdatacacheproxy.pas.models

import play.api.libs.json.{Json, OFormat}

case class CreateCisAppealRequest(
                                   penaltyId: Int
                                 )

object CreateCisAppealRequest {
  implicit val format: OFormat[CreateCisAppealRequest] = Json.format[CreateCisAppealRequest]
}

case class CreateCisAppealResponse(
                                    created: Boolean
                                  )

object CreateCisAppealResponse {
  implicit val format: OFormat[CreateCisAppealResponse] = Json.format[CreateCisAppealResponse]
}