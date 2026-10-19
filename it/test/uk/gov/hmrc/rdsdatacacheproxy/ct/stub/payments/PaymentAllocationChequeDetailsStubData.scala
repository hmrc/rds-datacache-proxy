package uk.gov.hmrc.rdsdatacacheproxy.ct.stub.payments

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.{PaymentAllocationChequeDetails, PaymentAllocationChequeRow}

import java.time.LocalDate

object PaymentAllocationChequeDetailsStubData {

  val paymentAllocationChequeDetailsRow: List[PaymentAllocationChequeRow] = List(
    PaymentAllocationChequeRow(
      accountingPeriod       = 12820832002L,
      apEndDate              = LocalDate.of(2016, 11, 9),
      amount                 = BigDecimal(7205720.982),
      effectiveDateOfPayment = LocalDate.of(2026, 11, 12)
    ),
    PaymentAllocationChequeRow(
      accountingPeriod       = 1232002L,
      apEndDate              = LocalDate.of(2017, 11, 9),
      amount                 = BigDecimal(7200.982),
      effectiveDateOfPayment = LocalDate.of(2026, 12, 12)
    )
  )

  val emptyPaymentAllocationChequeDetails: PaymentAllocationChequeDetails = PaymentAllocationChequeDetails(paymentAllocationDetails = List.empty)

  def getPaymentAllocationChequeDetails(taxRef: Long, chequeId: Long): PaymentAllocationChequeDetails = {
    (taxRef, chequeId) match {
      case (122323L, 16L)  => PaymentAllocationChequeDetails(paymentAllocationDetails = paymentAllocationChequeDetailsRow)
      case (9798232L, 37L) => throw new RuntimeException("Error from downstream")
      case (_, _)          => emptyPaymentAllocationChequeDetails
    }
  }

}
