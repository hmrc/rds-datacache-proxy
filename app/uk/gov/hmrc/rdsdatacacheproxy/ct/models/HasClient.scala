package uk.gov.hmrc.rdsdatacacheproxy.ct.models

import play.api.libs.json.{Json, OFormat}

case class HasClient(status: Int)

object HasClient {
  implicit val format: OFormat[HasClient] = Json.format[HasClient]
}