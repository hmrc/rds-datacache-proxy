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

package uk.gov.hmrc.rdsdatacacheproxy.ct.helpers

import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{CT600XmlDataResponse, FormListItem}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.FormDataRepository

import java.time.LocalDate
import scala.concurrent.Future

trait FormDataStub {

  val defaultDataItem = CT600XmlDataResponse(
    ct600XmlData = Some("data"),
    formList = List(
      FormListItem(
        formType = "A",
        xmlData  = Some("xml_data")
      )
    )
  )

  val firstDataItem = CT600XmlDataResponse(
    ct600XmlData = None,
    formList = List(
      FormListItem(
        formType = "A",
        xmlData  = Some("xml_data")
      )
    )
  )

  val emptyDataItem = CT600XmlDataResponse(
    ct600XmlData = None,
    formList = List(
      FormListItem(
        formType = "",
        xmlData  = None
      )
    )
  )

  val fullyEmptyDataItem = CT600XmlDataResponse(
    ct600XmlData = None,
    formList     = List.empty
  )

  class FormDataRdsStub extends FormDataRepository {

    def getData(taxRef: Long, accPeriod: Long, startDate: LocalDate, endDate: LocalDate): Future[CT600XmlDataResponse] = {
      (taxRef, accPeriod, startDate.toString, endDate.toString) match {
        case (1, 1, "2006-01-01", "2006-12-31") =>
          Future.successful(defaultDataItem)
        case (9, _, _, _) =>
          Future.successful(fullyEmptyDataItem)
        case (999, _, _, _) =>
          Future.successful(throw new Error("Upstream error"))
        case (_, _, _, _) =>
          Future.successful(emptyDataItem)
      }
    }

  }

}
