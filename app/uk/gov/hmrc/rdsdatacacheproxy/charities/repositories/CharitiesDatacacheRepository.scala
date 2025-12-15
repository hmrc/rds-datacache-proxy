/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.charities.repositories

import com.google.inject.ImplementedBy
import play.api.Logging
import play.api.db.Database

import java.sql.Types
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CharitiesDatacacheRepository])
trait CharitiesDataSource {
  def getAgentName(agentRef: String): Future[Option[String]]
  def getOrganisationName(charityRef: String): Future[Option[String]]
}

class CharitiesDatacacheRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends CharitiesDataSource with Logging {

  def getAgentName(agentRef: String): Future[Option[String]] = {
    logger.info(s"Input request, p_agent_ref: <$agentRef>")
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call CHAR_DC_AGENT_PK.getAgentName(?, ?)}")

        storedProcedure.setString("p_agent_ref", agentRef)
        storedProcedure.registerOutParameter("p_name", Types.VARCHAR)

        storedProcedure.execute()

        val agentName = storedProcedure.getString("p_name")

        storedProcedure.close()

        val result = Option(agentName)
        logger.info(s"Agent name from SQL Stored Procedure: ${result.getOrElse("null")}")
        result
      }
    }
  }

  def getOrganisationName(charityRef: String): Future[Option[String]] = {
    logger.info(s"Input request, p_charity_ref: <$charityRef>")
    Future {
      db.withConnection { connection =>
        val storedProcedure = connection.prepareCall("{call CHAR_DC_PK.getOrgName(?, ?)}")

        storedProcedure.setString("p_charity_ref", charityRef)
        storedProcedure.registerOutParameter("p_name", Types.VARCHAR)

        storedProcedure.execute()

        val organisationName = storedProcedure.getString("p_name")

        storedProcedure.close()

        val result = Option(organisationName)
        logger.info(s"Organisation name from SQL Stored Procedure: ${result.getOrElse("null")}")
        result
      }
    }
  }
}
