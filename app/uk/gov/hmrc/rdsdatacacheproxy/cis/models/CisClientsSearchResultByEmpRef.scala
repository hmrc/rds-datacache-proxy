package uk.gov.hmrc.rdsdatacacheproxy.cis.models

import play.api.libs.json.{Json, OFormat}

case class CisClientsSearchResultByEmpRef(
    clients: List[CisTaxpayerSearchResult],
    clientNameStartingCharacters: List[String]
)

object CisClientsSearchResultByEmpRef {
  implicit val format: OFormat[CisClientsSearchResultByEmpRef] = Json.format[CisClientsSearchResultByEmpRef]
}