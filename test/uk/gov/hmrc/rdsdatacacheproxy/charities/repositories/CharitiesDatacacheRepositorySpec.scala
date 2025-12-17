/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.rdsdatacacheproxy.charities.repositories

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database

import java.sql.{CallableStatement, Types}
import scala.concurrent.ExecutionContext.Implicits.global

class CharitiesDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: CharitiesDatacacheRepository = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _

  before {
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[java.sql.Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    repository = new CharitiesDatacacheRepository(db)
  }

  "getAgentName" should "return Some(agent name) when agent name exists" in {
    val agentRef = "AGENT123"
    val expectedName = "Test Agent Name"

    when(mockCallableStatement.getString("p_name")).thenReturn(expectedName)

    val result = repository.getAgentName(agentRef).futureValue

    result shouldBe Some(expectedName)

    verify(mockCallableStatement).setString("p_agent_ref", agentRef)
    verify(mockCallableStatement).registerOutParameter("p_name", Types.VARCHAR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getString("p_name")
    verify(mockCallableStatement).close()
  }

  "getAgentName" should "return None when agent name is null" in {
    val agentRef = "AGENT123"

    when(mockCallableStatement.getString("p_name")).thenReturn(null)

    val result = repository.getAgentName(agentRef).futureValue

    result shouldBe None

    verify(mockCallableStatement).setString("p_agent_ref", agentRef)
    verify(mockCallableStatement).registerOutParameter("p_name", Types.VARCHAR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getString("p_name")
    verify(mockCallableStatement).close()
  }

  "getOrganisationName" should "return Some(organisation name) when organisation name exists" in {
    val charityRef = "CHARITY123"
    val expectedName = "Test Organisation Name"

    when(mockCallableStatement.getString("p_name")).thenReturn(expectedName)

    val result = repository.getOrganisationName(charityRef).futureValue

    result shouldBe Some(expectedName)

    verify(mockCallableStatement).setString("p_charity_ref", charityRef)
    verify(mockCallableStatement).registerOutParameter("p_name", Types.VARCHAR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getString("p_name")
    verify(mockCallableStatement).close()
  }

  "getOrganisationName" should "return None when organisation name is null" in {
    val charityRef = "CHARITY123"

    when(mockCallableStatement.getString("p_name")).thenReturn(null)

    val result = repository.getOrganisationName(charityRef).futureValue

    result shouldBe None

    verify(mockCallableStatement).setString("p_charity_ref", charityRef)
    verify(mockCallableStatement).registerOutParameter("p_name", Types.VARCHAR)
    verify(mockCallableStatement).execute()
    verify(mockCallableStatement).getString("p_name")
    verify(mockCallableStatement).close()
  }
}
