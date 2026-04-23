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

package uk.gov.hmrc.rdsdatacacheproxy.gambling

import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.*
import java.time.LocalDate

object GamblingStubData {
  def getReturnSummary(mgdRegNumber: String): ReturnSummary = {
    mgdRegNumber match {
      case "XYZ00000000000" =>
        ReturnSummary(mgdRegNumber, returnsDue = 0, returnsOverdue = 0)
      case "XYZ00000000001" =>
        ReturnSummary(mgdRegNumber, returnsDue = 0, returnsOverdue = 1)
      case "XYZ00000000010" =>
        ReturnSummary(mgdRegNumber, returnsDue = 1, returnsOverdue = 0)
      case "XYZ00000000012" =>
        ReturnSummary(mgdRegNumber, returnsDue = 1, returnsOverdue = 2)
      case "XYZ00000000021" =>
        ReturnSummary(mgdRegNumber, returnsDue = 2, returnsOverdue = 1)
      case "ERR00000000000" =>
        throw new RuntimeException("Simulated downstream failure")
      case _ =>
        ReturnSummary(mgdRegNumber, returnsDue = 3, returnsOverdue = 4)
      }
    }
    def getBusinessName(mgdRegNumber: String): BusinessName = {
      val dateTimeOne: Some[LocalDate] = Some(LocalDate.of(2026, 4, 20))
      val dateTimeTwo: Some[LocalDate] = Some(LocalDate.of(2026, 1, 1))
      val dateTimeThree: Some[LocalDate] = Some(LocalDate.of(1991, 1, 1))
      mgdRegNumber match {
        case "XYZ00000000000" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Mr",
            solePropFirstName = "John",
            solePropMidName = "C",
            solePropLastName = "Doe",
            businessName = "John Doe Co.",
            businessType = "Sole Proprietor",
            tradingName = "DoeDoe",
            systemDate = dateTimeOne)
        case "XYZ00000000001" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Mrs",
            solePropFirstName = "Jane",
            solePropMidName = "C",
            solePropLastName = "Doe",
            businessName = "Jane Doe Co.",
            businessType = "Sole Proprietor",
            tradingName = "DoeDoe",
            systemDate = dateTimeTwo)
        case "XYZ00000000010" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Mrs",
            solePropFirstName = "Marge",
            solePropMidName = "Jacqueline",
            solePropLastName = "Simpson",
            businessName = "Pretzel Wagon",
            businessType = "Sole Proprietor",
            tradingName = "Marge Simpson",
            systemDate = dateTimeOne)
        case "XYZ00000000012" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Miss",
            solePropFirstName = "Catherine",
            solePropMidName = "",
            solePropLastName = "Havisham",
            businessName = "Failed Expectations",
            businessType = "Sole Proprietor",
            tradingName = "Miss Havisham",
            systemDate = dateTimeThree)
        case "XYZ00000000021" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Mr",
            solePropFirstName = "Eugine",
            solePropMidName = "H",
            solePropLastName = "Krabs",
            businessName = "Krusty Krab",
            businessType = "Sole Proprietor",
            tradingName = "Mr Krabs",
            systemDate = dateTimeThree)
        case "ERR00000000000" =>
          throw new RuntimeException("Simulated downstream failure")
        case _ =>
          BusinessName(mgdRegNumber,
            solePropTitle= "Mr",
            solePropFirstName= "Foo",
            solePropMidName= "B",
            solePropLastName= "Bar",
            businessName= "FooBar Co.",
            businessType= "Sole Proprietor",
            tradingName= "Foobar",
            systemDate= dateTimeOne
          )
      }
    }
  }

