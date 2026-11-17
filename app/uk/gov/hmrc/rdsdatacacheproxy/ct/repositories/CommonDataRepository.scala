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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CommonData

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CommonDataRepositoryImpl])
trait CommonDataRepository {
  def getCommonData(): Future[CommonData]
}

class CommonDataRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends CommonDataRepository
    with Logging {

  override def getCommonData(): Future[CommonData] = {
    logger.info("Invoking stored procedure to retrieve Common Data")

    Future {
      db.withConnection { connection =>

        val storedProcedure = connection.prepareCall(
          "{call CT_DC_PK.getCommonData(?)}"
        )

        try {
          storedProcedure.registerOutParameter(1, OracleTypes.DATE)

          storedProcedure.execute()

          CommonData(asOnDate = storedProcedure.getDate(1).toLocalDate)
        } finally storedProcedure.close()
      }
    }
  }
}
