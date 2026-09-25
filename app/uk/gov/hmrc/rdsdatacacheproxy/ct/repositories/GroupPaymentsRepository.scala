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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import com.google.inject.ImplementedBy
import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.Database
import play.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{GpaPaymentsDetails, GpaPaymentsItem, GroupReferenceNumberLstItem, GroupSummaryDetails, GroupSummaryDetailsItem}

import java.sql.ResultSet
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[GroupPaymentsRepositoryImpl])
trait GroupPaymentsRepository {
  def getGroupSummary(gpaUTR: Long, nomCompanyUTR: Long): Future[Option[GroupSummaryDetails]]

  def getPaymentsDetails(gpaUTR: Long, contractVersion: Int, startIndex: Int, count: Int): Future[Option[GpaPaymentsDetails]]
}

class GroupPaymentsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends GroupPaymentsRepository
    with RepositoryDataSupport
    with Logging {

  override def getGroupSummary(gpaUTR: Long, nomCompanyUTR: Long): Future[Option[GroupSummaryDetails]] = {
    logger.info(
      s"Retrieving getGroupSummary: $gpaUTR - $nomCompanyUTR"
    )
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("{call CT_GPA_PK.getGPAGroupSummary(?, ?, ?, ?, ?)}")

        try {
          cs.setLong(1, gpaUTR)
          cs.setLong(2, nomCompanyUTR)

          cs.registerOutParameter(3, OracleTypes.CURSOR)
          cs.registerOutParameter(4, OracleTypes.CURSOR)

          cs.registerOutParameter(5, java.sql.Types.VARCHAR) // pNOMINATED_COMPANY_NAME

          cs.execute()

          val curSummaryRds = cs.getObject(3, classOf[ResultSet])
          val groupRefsRds = cs.getObject(4, classOf[ResultSet])

          val curSummary = Option(curSummaryRds).map(readCurSummary).getOrElse(List.empty)
          val groupRefs = Option(groupRefsRds).map(readGroupRefs).getOrElse(List.empty)

          Some(
            GroupSummaryDetails(
              gpaGrpSummaryDetails  = curSummary,
              gpaReferenceNumberLst = groupRefs,
              nominatedCompanyName  = cs.getString(5)
            )
          )
        } catch {
          case sqlException: java.sql.SQLException if sqlException.getMessage.contains("no data found") =>
            logger.info("No data found")
            None // no other exceptions to be caught
        } finally {
          cs.close()
        }
      }
    }
  }

  private def readGroupRefs(rs: ResultSet): List[GroupReferenceNumberLstItem] = {
    val buffer = ListBuffer[GroupReferenceNumberLstItem]()
    while (rs.next()) {
      buffer += GroupReferenceNumberLstItem(
        taxpayerReference = Option(rs.getLong("taxpayer_reference")).get
      )
    }
    buffer.toList
  }

  private def readCurSummary(rs: ResultSet): List[GroupSummaryDetailsItem] = {
    val buffer = ListBuffer[GroupSummaryDetailsItem]()
    while (rs.next()) {
      buffer += GroupSummaryDetailsItem(
        contractEndDate         = Option(rs.getDate("CONTRACT_END_DATE")).map(_.toLocalDate).get,
        groupTaxCharge          = Option(rs.getBigDecimal("GROUP_TAX_CHARGE")),
        groupPayment            = Option(rs.getBigDecimal("GROUP_PAYMENT")),
        groupPaymentRecordCount = Option(rs.getInt("GROUP_PAYMENT_RECORD_COUNT")).get,
        contractStatus          = Option(rs.getString("CONTRACT_STATUS")).get,
        contractVersion         = Option(rs.getInt("CONTRACT_VERSION")).get
      )
    }
    buffer.toList
  }

  /*
   (pGPA_UTR                 IN     NUMBER,
                                 pGPP_CONTRACT_VERSION    IN     NUMBER,
                                 pSTART_INDEX             IN     NUMBER,
                                 pCOUNT                   IN     NUMBER,
				 pGPA_PAYMENTS		       OUT REF_CUR_TYPE,
                                 pTOTAL_NUM_RECORDS            OUT NUMBER,
                                 gGPP_END_DATE                 OUT DATE,
                                 pGPP_TOTAL_GROUP_PAYMENT      OUT NUMBER,
                                 pGPP_TOTAL_GROUP_TAX	       OUT NUMBER,
                                 pGPP_STATUS	               OUT VARCHAR,
                                 pGPP_CNI	               OUT DATE,
                                 pGPP_APPORTIONMENT_METHOD     OUT CHAR,
                                 pGPA_UTR_OUT                  OUT NUMBER,
                                 pGPP_CONTRACT_VERSION_OUT     OUT NUMBER)
   */

  override def getPaymentsDetails(gpaUTR: Long, contractVersion: Int, startIndex: Int, count: Int): Future[Option[GpaPaymentsDetails]] = {
    logger.info(
      s"Retrieving getPaymentsDetails: $gpaUTR - $contractVersion - $startIndex - $count"
    )
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("{call CT_GPA_PK.getGPAPaymentDetails(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )}")

        try {
          cs.setLong(1, gpaUTR)
          cs.setLong(2, contractVersion)
          cs.setLong(3, startIndex)
          cs.setLong(4, count)

          cs.registerOutParameter(5, OracleTypes.CURSOR)
          // other 6 - 14
          cs.registerOutParameter(6, java.sql.Types.INTEGER) // pTOTAL_NUM_RECORDS
          cs.registerOutParameter(7, java.sql.Types.DATE) // gGPP_END_DATE
          cs.registerOutParameter(8, java.sql.Types.DECIMAL) // pGPP_TOTAL_GROUP_PAYMENT
          cs.registerOutParameter(9, java.sql.Types.DECIMAL) // pGPP_TOTAL_GROUP_TAX
          cs.registerOutParameter(10, java.sql.Types.VARCHAR) // pGPP_STATUS
          cs.registerOutParameter(11, java.sql.Types.DATE) // pGPP_CNI
          cs.registerOutParameter(12, java.sql.Types.VARCHAR) // + pGPP_APPORTIONMENT_METHOD
          cs.registerOutParameter(13, java.sql.Types.INTEGER) // pGPA_UTR_OUT
          cs.registerOutParameter(14, java.sql.Types.INTEGER) // pGPP_CONTRACT_VERSION_OUT

          cs.execute()
          val curGpaPaymentsRds = cs.getObject(5, classOf[ResultSet])

          val curGpaPayments = Option(curGpaPaymentsRds).map(readGpaPayments).getOrElse(List.empty)

          Some(
            GpaPaymentsDetails(
              gpaPayments            = curGpaPayments,
              totalNumOfRecords      = Option(cs.getInt(6)),
              gppEndDate             = Option(cs.getDate(7)).map(_.toLocalDate),
              gppTotalGroupPayment   = Option(cs.getBigDecimal(8)),
              gppTotalGroupTax       = Option(cs.getBigDecimal(9)),
              gppStatus              = Option(cs.getString(10)).getOrElse(""),
              gppCni                 = Option(cs.getDate(11)).map(_.toLocalDate),
              gppApportionmentMethod = Option(cs.getString(12)).getOrElse("")
            )
          )
        } catch {
          case sqlException: java.sql.SQLException if sqlException.getMessage.contains("no data found") =>
            logger.info("No data found")
            None // no other exceptions to be caught
        } finally {
          cs.close()
        }
      }
    }
  }

  /*
     ROWNUM RN
	     ,DISPLAY_DATE
	     ,TOTAL
	     ,PAYMENT_TYPE
	     ,REPAYMENT_TYPE
	     ,TARGET_TAXPAYER_REFERENCE
	     ,TARGET_AP_NO
	     ,TARGET_AP_END_DATE
	     ,CONTRACT_END_DATE
	     ,PARTICIPATOR_COUNT
	     ,TABLENAME
   */
  private def readGpaPayments(rs: ResultSet): List[GpaPaymentsItem] = {
    val buffer = ListBuffer[GpaPaymentsItem]()
    while (rs.next()) {
      buffer += GpaPaymentsItem(
        displayDate             = Option(rs.getDate("DISPLAY_DATE")).map(_.toLocalDate),
        total                   = Option(rs.getBigDecimal("TOTAL")),
        tablename               = Option(rs.getString("TABLENAME")),
        targetTaxpayerReference = Option(rs.getString("TARGET_TAXPAYER_REFERENCE")),
        targetApNo              = Option(rs.getInt("TARGET_AP_NO")),
        targetApEndDate         = Option(rs.getDate("TARGET_AP_END_DATE")).map(_.toLocalDate),
        contractEndDate         = Option(rs.getDate("CONTRACT_END_DATE")).map(_.toLocalDate),
        participatorPresent     = Option(rs.getInt("PARTICIPATOR_COUNT")).map(_ > 0)
      )
    }
    buffer.toList
  }
}
