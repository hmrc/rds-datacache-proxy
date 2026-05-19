package uk.gov.hmrc.rdsdatacacheproxy.gambling.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class AssessmentsInAbsenceItem(
                                   dateProcessed: Option[LocalDate],
                                   amount: Option[BigDecimal]
                                 )

object AssessmentsInAbsenceItem {
  implicit val format: OFormat[AssessmentsInAbsenceItem] = Json.format[AssessmentsInAbsenceItem]
}

final case class AssessmentsInAbsence(
                                periodStartDate: Option[LocalDate],
                                periodEndDate: Option[LocalDate],
                                total: Option[BigDecimal],
                                totalRecords: Option[Int],
                                items: Seq[AssessmentsInAbsenceItem]
                              )

object AssessmentsInAbsence {
  implicit val format: OFormat[AssessmentsInAbsence] = Json.format[AssessmentsInAbsence]
}
