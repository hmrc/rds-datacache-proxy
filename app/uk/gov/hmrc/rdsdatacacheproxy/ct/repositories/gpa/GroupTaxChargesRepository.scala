/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa

import com.google.inject.ImplementedBy
import play.api.Logging
import play.api.db.Database
import play.db.NamedDatabase
import oracle.jdbc.OracleTypes
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.{GpaGroupTaxCharges, ParticipatorDetails}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.RepositoryDataSupport

import java.sql.ResultSet
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[GroupTaxChargesRepositoryImpl])
trait GroupTaxChargesRepository {
  def getGPAGroupTaxCharges(pGpaUtr: Long, pGppContractVersion: Long, pStartIndex: Long, pCount: Long): Future[GpaGroupTaxCharges]
}

class GroupTaxChargesRepositoryImpl @Inject() (@NamedDatabase("ct-core") db: Database)(implicit ec: ExecutionContext)
    extends GroupTaxChargesRepository
    with RepositoryDataSupport
    with Logging {

  override def getGPAGroupTaxCharges(pGpaUtr: Long, pGppContractVersion: Long, pStartIndex: Long, pCount: Long): Future[GpaGroupTaxCharges] = {
    val context = s"Retrieving getGPAGroupTaxCharges from GroupTaxChargesRepository "
    logger.info(s"Retrieving GpaGroupTaxCharges in GroupTaxChargesRepository for pGpaUtr: $pGpaUtr, pGppContractVersion: $pGppContractVersion")
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("call CT_GPA_PK.getGPAGroupTaxCharges(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
        try {
          cs.setLong(1, pGpaUtr)
          cs.setLong(2, pGppContractVersion)
          cs.setLong(3, pStartIndex)
          cs.setLong(4, pCount)

          cs.registerOutParameter(5, java.sql.Types.DATE) // pGPP_END_DATE
          cs.registerOutParameter(6, java.sql.Types.NUMERIC) // pGPP_TOTAL_GROUP_PAYMENT
          cs.registerOutParameter(7, java.sql.Types.NUMERIC) // pGPP_TOTAL_GROUP_TAX
          cs.registerOutParameter(8, java.sql.Types.VARCHAR) // pGPP_STATUS
          cs.registerOutParameter(9, java.sql.Types.DATE) // pGPP_CNI
          cs.registerOutParameter(10, java.sql.Types.CHAR) // pGPP_APPORTIONMENT_METHOD
          cs.registerOutParameter(11, java.sql.Types.NUMERIC) // pGPA_UTR2
          cs.registerOutParameter(12, java.sql.Types.NUMERIC) // pTOTAL_NUM_OF_RECORDS
          cs.registerOutParameter(13, java.sql.Types.NUMERIC) // pGROUP_PAYMENT_RECORD_COUNT
          cs.registerOutParameter(14, OracleTypes.CURSOR) // pCUR_GROUP_TAX_CHARGES
          cs.execute()

          val participatorDetails: List[ParticipatorDetails] = processResultSetList(cs, 14, processGroupTaxCharges, context)

          GpaGroupTaxCharges(
            pGppEndDate              = optDate(5, cs),
            pGppTotalGroupPayment    = optBigDecimal(6, cs),
            pGppTotalGroupTax        = optBigDecimal(7, cs),
            pGppStatus               = optString(8, cs),
            pGppCni                  = optDate(9, cs),
            pGppApportionmentMethod  = optString(10, cs),
            pGpaUtr2                 = cs.getLong(11),
            pTotalNumOfRecords       = optInt(12, cs),
            pGroupPaymentRecordCount = optInt(13, cs),
            pCurGroupTaxCharges      = participatorDetails
          )

        } finally cs.close()
      }
    }

  }

  private def processGroupTaxCharges(rs: ResultSet): ParticipatorDetails = {
    ParticipatorDetails(
      participatorName             = rs.getString("PARTICIPATOR_NAME"),
      participatorReference        = rs.getLong("PARTICIPATOR_REFERENCE"),
      participatorApEndDate        = rs.getDate("PARTICIPATOR_AP_END_DATE").toLocalDate,
      participatorTaxCharge        = rs.getBigDecimal("PARTICIPATOR_TAX_CHARGE"),
      participatorTaxChargePrsnt   = rs.getString("PARTICIPATOR_TAX_CHARGE_PRSNT"),
      participatorAccountingPeriod = rs.getLong("PARTICIPATOR_ACCOUNTING_PERIOD"),
      contractVersion              = rs.getLong("CONTRACT_VERSION"),
      allocatedPayment             = rs.getBigDecimal("ALLOCATED_PAYMENT"),
      allocatedPaymentRecordCount  = rs.getInt("ALLOCATED_PAYMENT_RECORD_COUNT")
    )
  }

}
