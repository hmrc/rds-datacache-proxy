package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PeriodWithinRange

object PeriodWithinRangeHelper {

  val periodWithinRangeFalse: PeriodWithinRange = PeriodWithinRange(
    isPeriodWithinRange = "N"
  )

  val periodWithinRangeTrue: PeriodWithinRange = PeriodWithinRange(
    isPeriodWithinRange = "Y"
  )

  def getGroupPaymentPeriodsInRange(gpaUTR: Long, nominatedCompanyUTR: Long, pPeriod: Long, pMonthRestriction: Long): PeriodWithinRange = {
    gpaUTR match {
      case 10L  => periodWithinRangeFalse
      case 20L  => periodWithinRangeTrue
      case 999L => throw new RuntimeException("Error from downstream")
      case _    => periodWithinRangeFalse
    }
  }

}
