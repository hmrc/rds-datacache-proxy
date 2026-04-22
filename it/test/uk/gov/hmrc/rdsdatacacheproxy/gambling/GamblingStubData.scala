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
import java.sql.Date

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
      val sqlDateOne: Date = Date.valueOf("2026-04-20")
      val sqlDateTwo: Date = Date.valueOf("2026-01-01")
      val sqlDateThree: Date = Date.valueOf("1992-01-01")
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
            systemDate = sqlDateOne)
        case "XYZ00000000001" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Mrs",
            solePropFirstName = "Jane",
            solePropMidName = "C",
            solePropLastName = "Doe",
            businessName = "Jane Doe Co.",
            businessType = "Sole Proprietor",
            tradingName = "DoeDoe",
            systemDate = sqlDateTwo)
        case "XYZ00000000010" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Mrs",
            solePropFirstName = "Marge",
            solePropMidName = "Jacqueline",
            solePropLastName = "Simpson",
            businessName = "Pretzel Wagon",
            businessType = "Sole Proprietor",
            tradingName = "Marge Simpson",
            systemDate = sqlDateOne)
        case "XYZ00000000012" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Miss",
            solePropFirstName = "Catherine",
            solePropMidName = "",
            solePropLastName = "Havisham",
            businessName = "Failed Expectations",
            businessType = "Sole Proprietor",
            tradingName = "Miss Havisham",
            systemDate = sqlDateThree)
        case "XYZ00000000021" =>
          BusinessName(mgdRegNumber,
            solePropTitle = "Mr",
            solePropFirstName = "Eugine",
            solePropMidName = "H",
            solePropLastName = "Krabs",
            businessName = "Krusty Krab",
            businessType = "Sole Proprietor",
            tradingName = "Mr Krabs",
            systemDate = sqlDateThree)
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
            systemDate= sqlDateOne
          )
      }
    }
  }

