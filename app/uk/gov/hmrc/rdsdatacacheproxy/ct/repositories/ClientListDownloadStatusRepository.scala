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

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ClientListDownloadStatusRepositoryImpl])
trait ClientListDownloadStatusRepository {
  def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int): Future[Int]
}

class ClientListDownloadStatusRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends ClientListDownloadStatusRepository
    with Logging {

  override def getClientListDownloadStatus(credentialId: String, serviceName: String, gracePeriod: Int): Future[Int] = {
    logger.info(s"Invoking stored procedure to retrieve Client List Download Status")
    Future {
      db.withConnection { connection =>

        val storedProcedure = connection.prepareCall("{call Client_List_Status.GetClientListDownloadStatus(?, ?, ?, ?)}")
        try {
          storedProcedure.setString(1, credentialId) // IN  P_CREDENTIAL_ID
          storedProcedure.setString(2, serviceName) // IN  P_SERVICE
          storedProcedure.setInt(3, gracePeriod) // IN  P_GRACEPERIOD
          storedProcedure.registerOutParameter(4, OracleTypes.INTEGER) // OUT P_STATUS
          storedProcedure.execute()

          storedProcedure.getInt(4)
        } finally storedProcedure.close()
      }
    }
  }
}
