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
import play.api.Logging
import play.api.db.Database
import play.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdminRule

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AdministrativeRuleRepositoryImpl])
trait AdministrativeRuleRepository {
  def getAdminRule(adminRuleKey: String): Future[AdminRule]
}

class AdministrativeRuleRepositoryImpl @Inject() (@NamedDatabase("ct-core") db: Database)(implicit ec: ExecutionContext)
    extends AdministrativeRuleRepository
    with RepositoryDataSupport
    with Logging {

  override def getAdminRule(adminRuleKey: String): Future[AdminRule] = {
    logger.info(s"[AdministrationRuleRepositoryImpl][getAdminRule] Calling stored procedure for adminRuleKey: $adminRuleKey")
    Future {
      db.withConnection { connection =>
        getAdminRuleFromDB(
          connection,
          adminRuleKey
        )
      }
    }
  }

  private def getAdminRuleFromDB(connection: Connection, adminRuleKey: String): AdminRule = {
    val cs = connection.prepareCall("{call CT_LNP_PK.getAdminRule(?, ?, ?)}")

    try {
      cs.setString(1, adminRuleKey)
      cs.registerOutParameter(2, java.sql.Types.NUMERIC) // P_ADMIN_RULE_NUMBER
      cs.registerOutParameter(3, java.sql.Types.DATE) // P_ADMIN_RULE_DATE

      cs.execute()

      AdminRule(
        ruleNumber = optBigDecimal(2, cs),
        ruleDate   = optDate(3, cs)
      )

    } finally {
      cs.close()
    }
  }

}
