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
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.*

import java.sql.{Connection, ResultSet, Types}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PasCisSource {
  def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse]
  def getCisAppealablePenalties(request: GetCisAppealablePenaltiesRequest): Future[GetCisAppealablePenaltiesResponse]
  def getCisAppealablePenalty(request: GetCisAppealablePenaltyRequest): Future[GetCisAppealablePenaltyResponse]
  def getCisPenaltyAllowed(request: GetCisPenaltyAllowedRequest): Future[GetCisPenaltyAllowedResponse]
}
class PasCisRepository @Inject() (
  @NamedDatabase("pas") db: Database
)(implicit ec: ExecutionContext)
    extends PasCisSource
    with RepositoryHelper {

  private val DefaultSortField = "unique_id"
  private val DefaultSortOrder = "desc"

  override def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse] = {
    Future {
      db.withTransaction { conn =>
        callCreateCisAppeal(
          conn         = conn,
          p_penalty_id = request.penaltyId
        )
      }
    }
  }

  private def callCreateCisAppeal(conn: Connection, p_penalty_id: Long): CreateCisAppealResponse = {

    val cs = conn.prepareCall("{call CIS_PAS.create_appeal(?)}")

    try {
      cs.setLong(1, p_penalty_id)

      cs.execute()

      CreateCisAppealResponse(
        created = true
      )

    } finally {
      cs.close()
    }
  }

  override def getCisAppealablePenalties(request: GetCisAppealablePenaltiesRequest): Future[GetCisAppealablePenaltiesResponse] = {
    Future {
      db.withConnection { conn =>
        callGetCisAppealablePenalties(
          conn                   = conn,
          p_hmrc_office_number   = request.hmrcOfficeNumber,
          p_cis_scheme_reference = request.schemeReferenceNumber,
          p_start_row            = request.startRow,
          p_end_row              = request.endRow
        )
      }
    }
  }

  private def callGetCisAppealablePenalties(conn: Connection,
                                            p_hmrc_office_number: String,
                                            p_cis_scheme_reference: String,
                                            p_start_row: Long,
                                            p_end_row: Long
                                           ): GetCisAppealablePenaltiesResponse = {

    val cs = conn.prepareCall("{call CIS_PAS.get_defaults_by_emp_ref(?, ?, ?, ?, ?, ?, ?, ?)}")

    try {
      cs.setString(1, p_hmrc_office_number)
      cs.setString(2, p_cis_scheme_reference)
      cs.setLong(3, p_start_row)
      cs.setLong(4, p_end_row)
      cs.setString(5, DefaultSortField)
      cs.setString(6, DefaultSortOrder)

      cs.registerOutParameter(7, OracleTypes.NUMERIC)
      cs.registerOutParameter(8, OracleTypes.CURSOR)
      cs.execute()

      val count = cs.getLong(7)
      val appealablePenaltiesList: Seq[GetCisAppealablePenaltyResponse] = processResultSetSeq(cs, 8, processAppealablePenalties)

      GetCisAppealablePenaltiesResponse(
        total               = count,
        appealablePenalties = appealablePenaltiesList
      )

    } finally {
      cs.close()
    }
  }

  private def processAppealablePenalties(rs: ResultSet): GetCisAppealablePenaltyResponse =
    GetCisAppealablePenaltyResponse(
      uniqueId         = Option(rs.getLong("UNIQUE_ID")),
      issueDate        = Option(rs.getDate("ISSUE_DATE")).map(_.toLocalDate),
      taxPeriodEndDate = Option(rs.getDate("TAX_PERIOD_END_DATE")).map(_.toLocalDate),
      penaltyType      = Option(rs.getString("PENALTY_TYPE")),
      penaltyAmount    = Option(rs.getBigDecimal("PENALTY_AMOUNT")).map(BigDecimal(_))
    )

  override def getCisAppealablePenalty(request: GetCisAppealablePenaltyRequest): Future[GetCisAppealablePenaltyResponse] = {
    Future {
      db.withConnection { conn =>
        callGetCisAppealablePenalty(
          conn                   = conn,
          p_penalty_id           = request.penaltyId,
          p_hmrc_office_number   = request.hmrcOfficeNumber,
          p_cis_scheme_reference = request.schemeReferenceNumber
        )
      }
    }
  }

  private def callGetCisAppealablePenalty(conn: Connection,
                                          p_penalty_id: Long,
                                          p_hmrc_office_number: String,
                                          p_cis_scheme_reference: String
                                         ): GetCisAppealablePenaltyResponse = {

    val cs = conn.prepareCall("{call CIS_PAS.get_appealable_penalty_by_id(?, ?, ?, ?, ?, ?, ?)}")

    try {
      cs.setString(1, p_hmrc_office_number)
      cs.setString(2, p_cis_scheme_reference)
      cs.setLong(3, p_penalty_id)

      cs.registerOutParameter(4, Types.VARCHAR) // p_default_type
      cs.registerOutParameter(5, Types.DATE) // p_issue_date
      cs.registerOutParameter(6, Types.NUMERIC) // p_penalty_amount
      cs.registerOutParameter(7, Types.DATE) // p_tax_period_end_date

      cs.execute()

      GetCisAppealablePenaltyResponse(
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

  override def getCisPenaltyAllowed(request: GetCisPenaltyAllowedRequest): Future[GetCisPenaltyAllowedResponse] = {
    Future {
      db.withConnection { conn =>
        callGetCisPenaltyAllowed(
          conn = conn,
          p_district_number = request.districtNumber,
          p_employer_ref = request.employerReference,
          p_unique_id = request.uniqueId
        )
      }
    }
  }

  private def callGetCisPenaltyAllowed(conn: Connection,
                                       p_district_number: Long,
                                       p_employer_ref: String,
                                       p_unique_id: Long
                                      ): GetCisPenaltyAllowedResponse = {

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
        case other => throw new IllegalStateException(s"Unexpected p_allowed value from DB: $other")
      }
      
      GetCisPenaltyAllowedResponse(allowed)

    } finally {
      cs.close()
    }
  }
}