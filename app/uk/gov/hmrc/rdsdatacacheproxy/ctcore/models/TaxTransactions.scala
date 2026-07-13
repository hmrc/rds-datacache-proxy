package uk.gov.hmrc.rdsdatacacheproxy.ctcore.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class TaxTransactions(taxTransactions: List[TaxTransactionsItem])
object TaxTransactions {
  implicit val format: OFormat[TaxTransactions] = Json.format[TaxTransactions]
}

case class TaxTransactionsItem(currentAmount: BigDecimal, assessmentType: String, taxDate: LocalDate, correctionClaimSignal: Option[String])
object TaxTransactionsItem {
  implicit val format: OFormat[TaxTransactionsItem] = Json.format[TaxTransactionsItem]
}
