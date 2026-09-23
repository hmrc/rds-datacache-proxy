package uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class GpaGroupTaxCharges(pGppEndDate:LocalDate,
                              pGppTotalGroupPayment:BigDecimal,
                              pGppTotalGroupTax: BigDecimal,
                              pGppStatus:String,
                              pGppCni:Option[LocalDate],
                              pGppApportionmentMethod: Option[String],
                              pGpaUtr2: Long,
                              pTotalNumOfRecords:Int,
                              pGroupPaymentRecordCount:Int,
                              pCurGroupTaxCharges:List[ParticipatorDetails])

object GpaGroupTaxCharges {
  implicit val format: OFormat[GpaGroupTaxCharges] = Json.format[GpaGroupTaxCharges]
}

case class ParticipatorDetails(participatorName:String,
                               participatorReference:Long,
                               participatorApEndDate:LocalDate,
                               participatorTaxCharge:BigDecimal,
                               participatorTaxChargePrsnt:String,
                               participatorAccountingPeriod:Long,
                               contractVersion:Long,
                               allocatedPayment:BigDecimal,
                               allocatedPaymentRecordCount:Int)

object ParticipatorDetails {
  implicit val format:OFormat[ParticipatorDetails] = Json.format[ParticipatorDetails]
}