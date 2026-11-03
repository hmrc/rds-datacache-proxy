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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.HasClient

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[HasClientRepositoryImpl])
trait HasClientRepository {
  def hasClient(utr: Long, irAgentId: String, credentialId: String): Future[HasClient]
}

class HasClientRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends HasClientRepository
    with Logging {

  override def hasClient(utr: Long, irAgentId: String, credentialId: String): Future[HasClient] = {
    logger.info("Invoking stored procedure to retrieve Has Client")

    Future {
      db.withConnection { connection =>

        val storedProcedure = connection.prepareCall(
          "{call CT_CLIENT_SEARCH.hasClient(?, ?, ?, ?)}"
        )

        try {
          storedProcedure.setLong(1, utr)
          storedProcedure.setString(2, irAgentId)
          storedProcedure.setString(3, credentialId)
          storedProcedure.registerOutParameter(4, OracleTypes.INTEGER)

          storedProcedure.execute()

          HasClient(status = storedProcedure.getInt(4))
        } finally storedProcedure.close()
      }
    }
  }
}
