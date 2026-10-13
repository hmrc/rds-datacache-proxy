package uk.gov.hmrc.rdsdatacacheproxy.pas.repositories


import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdsdatacacheproxy.pas.models.{CreateCisAppealRequest, CreateCisAppealResponse}

import java.sql.Connection
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait CisSource {
  def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse]
}
class CisRepository @Inject() (
                                @NamedDatabase("pas") db: Database
                              )(implicit ec: ExecutionContext)
  extends CisSource
    with Logging {

  override def createCisAppeal(request: CreateCisAppealRequest): Future[CreateCisAppealResponse] = {
    Future {
      db.withTransaction { conn =>
        callCreateCisAppeal(
          conn = conn,
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
