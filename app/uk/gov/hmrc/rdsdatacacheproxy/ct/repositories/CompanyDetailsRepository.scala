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
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CompanyDetails, CompanyDetailsResponse}

import java.sql.{Connection, ResultSet}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CompanyDetailsRepositoryImpl])
trait CompanyDetailsRepository {
  def getCompanyDetails(taxPayerReference: Long): Future[CompanyDetailsResponse]
}

class CompanyDetailsRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends CompanyDetailsRepository
    with RepositoryDataSupport
    with Logging {

  override def getCompanyDetails(taxPayerReference: Long): Future[CompanyDetailsResponse] =
    logger.info(s"Invoking stored procedure to retrieve CompanyDetailsResponse for taxRef :: $taxPayerReference")
    Future {
      db.withConnection { connection =>
        retrieveCompanyDetails(connection, taxPayerReference)
      }
    }

  private def retrieveCompanyDetails(connection: Connection, taxPayerReference: Long): CompanyDetailsResponse = {
    val context: String = s"Retrieving CompanyDetails"

    val cs = connection.prepareCall("{call UDAS_CT_ODS_DC.getCTTaxpayer(?,?)}")

    try {
      cs.setLong(1, taxPayerReference)

      cs.registerOutParameter(2, OracleTypes.CURSOR)

      cs.execute()

      val companyDetails: List[CompanyDetails] = processResultSetList(cs, 2, processCompanyDetails, context)

      CompanyDetailsResponse(
        taxpayerDetails = companyDetails
      )

    } finally {
      cs.close()
    }
  }

  private def processCompanyDetails(rs: ResultSet): CompanyDetails = {
    CompanyDetails(
      rs.getString("ORG_UNIT_ID"),
      rs.getString("COMPANY_NAME"),
      Option(rs.getString("COMPANY_REG_NO")),
      Option(rs.getString("ADDRESS_LINE1")),
      Option(rs.getString("ADDRESS_LINE2")),
      Option(rs.getString("ADDRESS_LINE3")),
      Option(rs.getString("ADDRESS_LINE4")),
      Option(rs.getString("POST_CODE"))
    )

  }
}
