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
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CompanyNominator
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CompanyNominatorRepositoryImpl])
trait CompanyNominatorRepository {
  def getIsCompanyNominatorOfGPA(gpaUtr: Long, nominatedCompanyUtr: Long): Future[CompanyNominator]
}

class CompanyNominatorRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends CompanyNominatorRepository
    with RepositoryDataSupport
    with Logging {

  def getIsCompanyNominatorOfGPA(gpaUtr: Long, nominatedCompanyUtr: Long): Future[CompanyNominator] = {
    Future {
      db.withConnection { connect =>

        val storedProcedure = connect.prepareCall("{call CT_GPA_PK.isCompanyNominatorOfGPA(?, ?, ?)}")

        try {
          storedProcedure.setLong(1, gpaUtr)
          storedProcedure.setLong(2, nominatedCompanyUtr)

          storedProcedure.registerOutParameter(3, java.sql.Types.VARCHAR)

          storedProcedure.execute()

          CompanyNominator(
            isParticipator = storedProcedure.getString(3)
          )

        } finally {
          storedProcedure.close()
        }
      }
    }
  }
}
