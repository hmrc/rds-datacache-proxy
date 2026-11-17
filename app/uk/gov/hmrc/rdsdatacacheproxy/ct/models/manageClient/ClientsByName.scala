package uk.gov.hmrc.rdsdatacacheproxy.ct.models.manageClient

import play.api.libs.json.{Json, OFormat}

case class ClientsByName(
                       clientCount: Int,
                       clientList: List[ClientList],
                       clientNameStartChars: List[String]
                     )

object ClientsByName {
  implicit val format: OFormat[ClientsByName] = Json.format[ClientsByName]
}

case class ClientList(
                       utr: String,
                       companyRegNo: String,
                       companyName: String,
                       agentOwnRef: String
                     )

object ClientList {
  implicit val format: OFormat[ClientList] = Json.format[ClientList]
}