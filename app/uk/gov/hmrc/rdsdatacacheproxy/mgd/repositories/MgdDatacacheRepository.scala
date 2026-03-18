package uk.gov.hmrc.rdsdatacacheproxy.mgd.repositories

import uk.gov.hmrc.rdsdatacacheproxy.mgd.models.ReturnSummary
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import oracle.jdbc.OracleTypes
import java.sql.{CallableStatement, Date, ResultSet, Types}
import java.time.LocalDate
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.Future

trait MgdDataSource {
  def getReturnSummary(mgd_reg_number: String, returns_due: Int, returns_overdue: Int): Future[Option[ReturnSummary]]
}

@Singleton
class MgdDatacacheRepository @Inject() (
                                          @NamedDatabase("Mgd") db: Database
                                        )(implicit ec: ExecutionContext)
  extends MgdDataSource
    with Logging {

  override def getReturnSummary(mgd_reg_number: String, returns_due: Int, returns_overdue: Int): Future[Option[ReturnSummary]] = {
    logger.info(s"[Mgd] getReturnSummary(mgd_reg_number=$mgd_reg_number, returns_due=$returns_due, returns_overdue=$returns_overdue)")

    Future {
      db.withConnection { conn =>
        val cs: CallableStatement =
          conn.prepareCall("{ call MGD_DC_RTN_PCK.GET_RETURN_SUMMARY(?, ?, ?) }")

        try {
          
        } finally cs.close()
      }
    }
  }
}