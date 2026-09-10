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

package uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories

import play.api.Logging
import play.api.db.NamedDatabase
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.Regime
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.RepositorySupport.{GTRDatabase, MGDDatabase}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait UpdateStatusPeriodDataSource {
  def updateStatusPeriod(regNumber: String, consecNo: Int, status: Int): Future[Unit]
}

@Singleton
class UpdateStatusPeriodDataCacheRepository @Inject() (
  @NamedDatabase("gambling") mgdDb: MGDDatabase,
  @NamedDatabase("gambling.gtr") gtrDb: GTRDatabase
)(implicit ec: ExecutionContext)
    extends UpdateStatusPeriodDataSource
    with RepositorySupport
    with Logging {

  override def updateStatusPeriod(regNumber: String, consecNo: Int, status: Int): Future[Unit] =
    Future {
      getDb(Regime.MGD, mgdDb, gtrDb).underlying.withConnection { connection =>
        val cs = connection.prepareCall("{ call MGD_DC_RTN_PCK.SET_PERIOD_STATUS(?, ?, ?) }")
        try {
          cs.setString(1, regNumber) // IN P_MGD_REG_NUMBER
          cs.setInt(2, consecNo) // IN P_CONSEC_NO
          cs.setInt(3, status) // IN P_STATUS
          cs.execute()
          ()
        } finally closeQuietly(cs)
      }
    }(ec)
}
