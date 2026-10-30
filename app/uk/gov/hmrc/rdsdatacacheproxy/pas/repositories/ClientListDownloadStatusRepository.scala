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

import java.sql.CallableStatement
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait ClientListSource {
  def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int = 14400): Future[Int]
}
class ClientListDownloadStatusRepository @Inject() (
  @NamedDatabase("cis") dbCis: Database,
  @NamedDatabase("paye") dbPaye: Database
)(implicit ec: ExecutionContext)
    extends ClientListSource
    with Logging {

  override def getClientListDownloadStatus(
    credentialId: String,
    serviceName: String,
    gracePeriod: Int
  ): Future[Int] = {
    logger.info(s"[PAS] getClientListDownloadStatus(CREDENTIAL_ID=$credentialId, SERVICE_NAME=$serviceName, GRACE_PERIOD=$gracePeriod)")

    val db = serviceName.toLowerCase match {
      case "cis" => dbCis
      case _     => dbPaye
    }

    Future {
      db.withConnection { conn =>
        val getDownloadStatusSql: CallableStatement =
          conn.prepareCall("{ call CLIENT_LIST_STATUS.GETCLIENTLISTDOWNLOADSTATUS(?, ?, ?, ?) }")

        try {
          getDownloadStatusSql.setString(1, credentialId)
          getDownloadStatusSql.setString(2, serviceName)
          getDownloadStatusSql.setInt(3, gracePeriod)
          getDownloadStatusSql.registerOutParameter(4, OracleTypes.INTEGER)
          getDownloadStatusSql.execute()

          getDownloadStatusSql.getInt(4)
        } finally getDownloadStatusSql.close()
      }
    }
  }

}
