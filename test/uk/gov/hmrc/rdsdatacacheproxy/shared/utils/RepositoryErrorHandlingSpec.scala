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

package uk.gov.hmrc.rdsdatacacheproxy.shared.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.sql.SQLException

class RepositoryErrorHandlingSpec extends AnyFlatSpec with Matchers with RepositoryErrorHandling {

  private def run(f: => Either[RepositoryError, String]): Either[RepositoryError, String] =
    try f
    catch handleError[String]("QUERY_NAME", "KEY")

  "oracleErrorHandler" should "return Right when nothing is thrown" in {
    run(Right("ok")) shouldBe Right("ok")
  }

  it should "map ORA-01403 to RecordNotFound" in {
    run(throw new SQLException("ORA-01403: no data found", "02000", 1403)) match {
      case Left(RecordNotFound(msg)) => msg should (include("QUERY_NAME") and include("KEY"))
      case other                     => fail(s"expected Left(RecordNotFound), got $other")
    }
  }

  it should "match on the error code, not the message" in {
    run(throw new SQLException("something else", "02000", 1403)).left.toOption.get          shouldBe a[RecordNotFound]
    run(throw new SQLException("ORA-01403: no data found", "02000", 942)).left.toOption.get shouldBe a[DatabaseError]
  }

  it should "map any other SQLException to DatabaseError" in {
    val ex = new SQLException("ORA-00942: table or view does not exist", "42000", 942)
    run(throw ex) match {
      case Left(DatabaseError(msg, cause)) =>
        msg     should (include("QUERY_NAME") and include("KEY"))
        cause shouldBe ex
      case other => fail(s"expected Left(DatabaseError), got $other")
    }
  }

  it should "map a non-SQL exception to DatabaseError" in {
    val ex = new RuntimeException("boom")
    run(throw ex) match {
      case Left(DatabaseError(_, cause)) => cause shouldBe ex
      case other                         => fail(s"expected Left(DatabaseError), got $other")
    }
  }
}
