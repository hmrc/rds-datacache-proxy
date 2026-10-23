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

import java.sql.{Connection, ResultSet}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PasPayeSource {
  def getPayeAppealablePenalties(request: GetAppealablePenaltiesRequest): Future[GetAppealablePenaltiesResponse]
}
class PasPayeRepository @Inject() (
  @NamedDatabase("pas") db: Database
)(implicit ec: ExecutionContext)
    extends PasPayeSource
    with RepositoryHelper {

  private val DefaultSortField = "unique_id"
  private val DefaultSortOrder = "desc"

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

}
