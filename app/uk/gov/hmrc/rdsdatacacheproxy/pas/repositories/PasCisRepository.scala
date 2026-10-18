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

import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse}

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PasCisSource {
  def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse]
}
class PasCisRepository @Inject() (
  @NamedDatabase("pas") db: Database
)(implicit ec: ExecutionContext)
    extends PasCisSource
    with Logging {

  override def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse] = {
    Future {
      db.withTransaction { conn =>
        callCreateCisAppeal(
          conn         = conn,
          p_penalty_id = request.penaltyId
        )
      }
    }
  }

  private def callCreateCisAppeal(conn: Connection, p_penalty_id: Long): CreateCisAppealResponse = {

    val cs = conn.prepareCall("{call CIS_PAS.create_appeal(?)}")

    try {
      cs.setLong(1, p_penalty_id)

      cs.execute()

      CreateCisAppealResponse(
        created = true
      )

    } finally {
      cs.close()
    }
  }
}
