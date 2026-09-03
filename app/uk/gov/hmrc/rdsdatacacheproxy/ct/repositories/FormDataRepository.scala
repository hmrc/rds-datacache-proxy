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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CT600XmlDataResponse, FormListItem}

import java.sql.{Date, ResultSet}
import java.time.LocalDate
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Codec

@ImplementedBy(classOf[FormDataRepositoryImpl])
trait FormDataRepository {
  def getData(taxRef: Long, accPeriod: Long, startDate: LocalDate, endDate: LocalDate): Future[Option[CT600XmlDataResponse]]
}

class FormDataRepositoryImpl @Inject() (
  @NamedDatabase("ct-core") db: Database
)(implicit ec: ExecutionContext)
    extends FormDataRepository
    with Logging {

  /*
    p_taxpayer_reference    IN    accounting_period.taxpayer_reference%TYPE,
   p_accounting_period     IN    accounting_period.accounting_period%TYPE,
   p_ap_start_date         IN    accounting_period.ap_start_date%TYPE,
   p_ap_end_date           IN    accounting_period.ap_end_date%TYPE,
   ct600_xml_data          OUT   ct600_base_data.xml_data%TYPE,
   form_list               OUT   cursor_type) IS
   */
  def getData(taxRef: Long, accPeriod: Long, startDate: LocalDate, endDate: LocalDate): Future[Option[CT600XmlDataResponse]] = {
    logger.info(s"Input request: taxRef, accPeriod, startDate, endDate: <$taxRef>, <$accPeriod> <$startDate> <$endDate>")
    Future {
      db.withConnection { connection =>
        val cs = connection.prepareCall("{call UDAS_CT_DC.getLatestCTReturn(?, ?, ?, ?, ?, ?)}")
        try {
          cs.setLong(1, taxRef)
          cs.setLong(2, accPeriod)
          cs.setDate(3, Date.valueOf(startDate))
          cs.setDate(4, Date.valueOf(endDate))

          cs.registerOutParameter(5, java.sql.Types.CLOB)
          cs.registerOutParameter(6, OracleTypes.CURSOR)

          cs.execute()
          val result = cs.getObject(6, classOf[ResultSet])

          val formListItems = Option(result).map(readFormItems).getOrElse(List.empty)

          Some(
            CT600XmlDataResponse(
              ct600XmlData = Option(cs.getClob(5))
                .map(clob => scala.io.Source.fromInputStream(clob.getAsciiStream).getLines().mkString),
              formList = formListItems
            )
          )
        } finally {
          cs.close()
        }
      }
    }
  }

  private def readFormItems(rs: ResultSet): List[FormListItem] = {
    val buffer = ListBuffer[FormListItem]()
    while (rs.next()) {
      buffer += FormListItem(
        formType = rs.getString("form_type"),
        xmlData  = Option(rs.getString("xml_data"))
      )
    }
    buffer.toList
  }

}
