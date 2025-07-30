package uk.gov.hmrc.rdsdatacacheproxy.models.requests

import play.api.libs.json.{Json, OFormat}

case class WorkingDaysOffsetRequest(baseDate: String, offsetWorkingDays: String)

object WorkingDaysOffsetRequest {
  implicit val format: OFormat[WorkingDaysOffsetRequest] = Json.format
}
