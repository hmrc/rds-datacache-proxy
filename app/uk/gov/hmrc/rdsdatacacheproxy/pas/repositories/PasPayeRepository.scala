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

package uk.gov.hmrc.rdsdatacacheproxy.pas.repositories

import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*

import java.sql.{Connection, ResultSet, Types}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PasPayeSource {
  def createPayeAppeal(request: CreateAppealRequest): Future[CreateAppealResponse]
  def getPayeAppealablePenalties(request: GetAppealablePenaltiesRequest): Future[GetAppealablePenaltiesResponse]
  def getPayeAppealablePenalty(request: GetAppealablePenaltyRequest): Future[GetAppealablePenaltyResponse]
  def getPayeTaxpayer(request: GetTaxpayerRequest): Future[Option[GetPayeTaxpayerResponse]]
  def getPayePenaltyAllowed(request: GetPenaltyAllowedRequest): Future[GetPenaltyAllowedResponse]
}
class PasPayeRepository @Inject() (
  @NamedDatabase("pas") db: Database
)(implicit ec: ExecutionContext)
    extends PasPayeSource
    with RepositoryHelper
    with Logging {

  private val DefaultSortField = "unique_id"
  private val DefaultSortOrder = "desc"

  override def createPayeAppeal(request: CreateAppealRequest): Future[CreateAppealResponse] = {
    Future {
      db.withTransaction { conn =>
        callCreatePayeAppeal(
          conn         = conn,
          p_penalty_id = request.penaltyId
        )
      }
    }
  }

  private def callCreatePayeAppeal(conn: Connection, p_penalty_id: Long): CreateAppealResponse = {

    val cs = conn.prepareCall("{call PAYE_PAS.create_appeal(?)}")

    try {
      cs.setLong(1, p_penalty_id)

      cs.execute()

      CreateAppealResponse(
        created = true
      )

    } finally {
      cs.close()
    }
  }

  override def getPayeAppealablePenalties(request: GetAppealablePenaltiesRequest): Future[GetAppealablePenaltiesResponse] = {
    Future {
      db.withConnection { conn =>
        callGetPayeAppealablePenalties(
          conn                    = conn,
          p_hmrc_office_number    = request.hmrcOfficeNumber,
          p_paye_scheme_reference = request.schemeReferenceNumber,
          p_start_row             = request.startRow,
          p_end_row               = request.endRow
        )
      }
    }
  }

  private def callGetPayeAppealablePenalties(conn: Connection,
                                             p_hmrc_office_number: String,
                                             p_paye_scheme_reference: String,
                                             p_start_row: Long,
                                             p_end_row: Long
                                            ): GetAppealablePenaltiesResponse = {

    val cs = conn.prepareCall("{call PAYE_PAS.get_defaults_by_emp_ref(?, ?, ?, ?, ?, ?, ?, ?)}")

    try {
      cs.setString(1, p_hmrc_office_number)
      cs.setString(2, p_paye_scheme_reference)
      cs.setLong(3, p_start_row)
      cs.setLong(4, p_end_row)
      cs.setString(5, DefaultSortField)
      cs.setString(6, DefaultSortOrder)

      cs.registerOutParameter(7, OracleTypes.NUMERIC)
      cs.registerOutParameter(8, OracleTypes.CURSOR)
      cs.execute()

      val count = cs.getLong(7)
      val appealablePenaltiesList: Seq[GetAppealablePenaltyResponse] = processResultSetSeq(cs, 8, processAppealablePenalties)

      GetAppealablePenaltiesResponse(
        total               = count,
        appealablePenalties = appealablePenaltiesList
      )

    } finally {
      cs.close()
    }
  }

  private def processAppealablePenalties(rs: ResultSet): GetAppealablePenaltyResponse =
    GetAppealablePenaltyResponse(
      uniqueId         = Option(rs.getLong("UNIQUE_ID")),
      issueDate        = Option(rs.getDate("ISSUE_DATE")).map(_.toLocalDate),
      taxPeriodEndDate = Option(rs.getDate("TAX_PERIOD_END_DATE")).map(_.toLocalDate),
      penaltyType      = Option(rs.getString("DEFAULT_TYPE")),
      penaltyAmount    = Option(rs.getBigDecimal("PENALTY_AMOUNT")).map(BigDecimal(_))
    )

  override def getPayeAppealablePenalty(request: GetAppealablePenaltyRequest): Future[GetAppealablePenaltyResponse] = {
    Future {
      db.withConnection { conn =>
        callGetPayeAppealablePenalty(
          conn                    = conn,
          p_penalty_id            = request.penaltyId,
          p_hmrc_office_number    = request.hmrcOfficeNumber,
          p_paye_scheme_reference = request.schemeReferenceNumber
        )
      }
    }
  }

  private def callGetPayeAppealablePenalty(conn: Connection,
                                           p_penalty_id: Long,
                                           p_hmrc_office_number: String,
                                           p_paye_scheme_reference: String
                                          ): GetAppealablePenaltyResponse = {

    val cs = conn.prepareCall("{call PAYE_PAS.get_appealable_penalty_by_id(?, ?, ?, ?, ?, ?, ?)}")

    try {
      cs.setString(1, p_hmrc_office_number)
      cs.setString(2, p_paye_scheme_reference)
      cs.setLong(3, p_penalty_id)

      cs.registerOutParameter(4, Types.VARCHAR) // p_default_type
      cs.registerOutParameter(5, Types.DATE) // p_issue_date
      cs.registerOutParameter(6, Types.NUMERIC) // p_penalty_amount
      cs.registerOutParameter(7, Types.DATE) // p_tax_period_end_date

      cs.execute()

      GetAppealablePenaltyResponse(
        uniqueId         = None,
        issueDate        = Option(cs.getDate(5)).map(_.toLocalDate),
        taxPeriodEndDate = Option(cs.getDate(7)).map(_.toLocalDate),
        penaltyType      = Option(cs.getString(4)),
        penaltyAmount    = Option(cs.getBigDecimal(6)).map(BigDecimal(_))
      )

    } finally {
      cs.close()
    }
  }

  override def getPayeTaxpayer(request: GetTaxpayerRequest): Future[Option[GetPayeTaxpayerResponse]] = {
    Future {
      db.withConnection { conn =>
        callGetPayeTaxpayer(
          conn              = conn,
          p_district_number = request.hmrcOfficeNumber,
          p_employer_ref    = request.schemeReferenceNumber
        )
      }
    }
  }

  private def callGetPayeTaxpayer(conn: Connection, p_district_number: String, p_employer_ref: String): Option[GetPayeTaxpayerResponse] = {

    val cs = conn.prepareCall("{call PAYE_KF_PK.getPayeTaxpayerByTaxReference(?, ?, ?)}")

    try {
      cs.setString(1, p_district_number)
      cs.setString(2, p_employer_ref)

      cs.registerOutParameter(3, OracleTypes.CURSOR)
      cs.execute()

      val rs = cs.getObject(3, classOf[ResultSet])
      try
        if (rs != null && rs.next()) {
          val first = processPayeTaxpayer(rs)

          if (rs.next()) {
            logger.warn(
              s"[getPayeTaxpayer] multiple rows for districtNumber=$p_district_number, employerRef=$p_employer_ref; using first row (UNIQUEID=${first.uniqueId})"
            )
          }
          Some(first)
        } else None
      finally if (rs != null) rs.close()

    } finally {
      cs.close()
    }
  }

  // DISTRICT_NUMBER is a NUMBER column so leading zeros are lost (070 -> 70); a district number is always 3 digits
  private def districtNumber(rs: ResultSet): String =
    Option(rs.getString("DISTRICT_NUMBER"))
      .map(d => if (d.nonEmpty && d.forall(_.isDigit)) d.reverse.padTo(3, '0').reverse else d)
      .getOrElse("")

  private def processPayeTaxpayer(rs: ResultSet): GetPayeTaxpayerResponse =
    GetPayeTaxpayerResponse(
      uniqueId       = rs.getLong("UNIQUEID"),
      instanceId     = rs.getLong("INSTANCE_ID"),
      districtNumber = districtNumber(rs),
      employerRef    = Option(rs.getString("EMPLOYER_REF")).getOrElse(""),
      employerName1  = Option(rs.getString("EMPLOYER_NAME1")),
      employerName2  = Option(rs.getString("EMPLOYER_NAME2")),
      agentOwnRef    = Option(rs.getString("AGENT_OWN_REF"))
    )

  override def getPayePenaltyAllowed(request: GetPenaltyAllowedRequest): Future[GetPenaltyAllowedResponse] = {
    Future {
      db.withConnection { conn =>
        callGetPayePenaltyAllowed(
          conn              = conn,
          p_district_number = request.districtNumber,
          p_employer_ref    = request.employerReference,
          p_unique_id       = request.uniqueId
        )
      }
    }
  }

  private def callGetPayePenaltyAllowed(conn: Connection,
                                        p_district_number: Long,
                                        p_employer_ref: String,
                                        p_unique_id: Long
                                       ): GetPenaltyAllowedResponse = {

    val cs = conn.prepareCall("{call CIS_PAS.isPenaltyAccessAllowed(?, ?, ?, ?)}")

    try {
      cs.setLong(1, p_district_number)
      cs.setString(2, p_employer_ref)
      cs.setLong(3, p_unique_id)

      cs.registerOutParameter(4, Types.VARCHAR) // p_allowed

      cs.execute()

      val allowed: Boolean = Option(cs.getString(4)).map(_.trim.toUpperCase) match {
        case Some("Y") => true
        case Some("N") => false
        case other     => throw new IllegalStateException(s"Unexpected p_allowed value from DB: $other")
      }

      GetPenaltyAllowedResponse(allowed)

    } finally {
      cs.close()
    }
  }
}
