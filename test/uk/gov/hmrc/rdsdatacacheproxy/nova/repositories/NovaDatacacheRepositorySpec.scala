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

package uk.gov.hmrc.rdsdatacacheproxy.nova.repositories

import oracle.jdbc.OracleTypes
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database

import uk.gov.hmrc.rdsdatacacheproxy.nova.models.{EuMemberStatesResponse, TraderInformation, VehicleStatusDetails}

import java.math.BigDecimal as JBigDecimal
import java.sql.{CallableStatement, Date, ResultSet, Types}
import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class NovaDatacacheRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: NovaDatacacheRepository = _
  var mockConnection: java.sql.Connection = _
  var mockCs: CallableStatement = _

  before {
    db             = mock(classOf[Database])
    mockConnection = mock(classOf[java.sql.Connection])
    mockCs         = mock(classOf[CallableStatement])

    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[java.sql.Connection => Any])
      func(mockConnection)
    }

    when(mockConnection.prepareCall(any[String])).thenReturn(mockCs)

    repository = new NovaDatacacheRepository(db)
  }

  // ─── getTraderDetails ────────────────────────────────────────────────────────

  "getTraderDetails" should "return Some(TraderDetailsResponse) with userTrader and no clientTrader when userVrn only" in {
    val userInfoRs = mock(classOf[ResultSet])
    val userAddrRs = mock(classOf[ResultSet])
    val userDetailRs = mock(classOf[ResultSet])
    val emptyRs = mock(classOf[ResultSet])

    when(mockCs.getObject(3, classOf[ResultSet])).thenReturn(userInfoRs)
    when(mockCs.getObject(4, classOf[ResultSet])).thenReturn(userAddrRs)
    when(mockCs.getObject(5, classOf[ResultSet])).thenReturn(emptyRs)
    when(mockCs.getObject(6, classOf[ResultSet])).thenReturn(emptyRs)
    when(mockCs.getObject(7, classOf[ResultSet])).thenReturn(userDetailRs)
    when(mockCs.getObject(8, classOf[ResultSet])).thenReturn(emptyRs)

    when(userInfoRs.next()).thenReturn(true)
    when(userAddrRs.next()).thenReturn(true)
    when(userDetailRs.next()).thenReturn(true)
    when(emptyRs.next()).thenReturn(false)

    stubTraderInfoRs(userInfoRs)
    stubAddrContactRs(userAddrRs)
    stubTraderDetailsRs(userDetailRs)

    val result = repository.getTraderDetails("123456789", None).futureValue

    result                              shouldBe defined
    result.get.userTrader.vrn           shouldBe "123456789"
    result.get.userTrader.traderName    shouldBe Some("Test Trader Ltd")
    result.get.userTrader.postcode      shouldBe Some("TE1 1ST")
    result.get.userTrader.redundant     shouldBe false
    result.get.userTrader.insolvent     shouldBe false
    result.get.userTrader.missingTrader shouldBe false
    result.get.clientTrader             shouldBe None

    verify(mockCs).setString(1, "123456789")
    verify(mockCs).setNull(2, Types.VARCHAR)
    verify(mockCs).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(5, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(6, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(7, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(8, OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  "getTraderDetails" should "return Some(TraderDetailsResponse) with both userTrader and clientTrader when clientVrn is provided" in {
    val userInfoRs = mock(classOf[ResultSet])
    val userAddrRs = mock(classOf[ResultSet])
    val clientInfoRs = mock(classOf[ResultSet])
    val clientAddrRs = mock(classOf[ResultSet])
    val userDetailRs = mock(classOf[ResultSet])
    val clientDetailRs = mock(classOf[ResultSet])

    when(mockCs.getObject(3, classOf[ResultSet])).thenReturn(userInfoRs)
    when(mockCs.getObject(4, classOf[ResultSet])).thenReturn(userAddrRs)
    when(mockCs.getObject(5, classOf[ResultSet])).thenReturn(clientInfoRs)
    when(mockCs.getObject(6, classOf[ResultSet])).thenReturn(clientAddrRs)
    when(mockCs.getObject(7, classOf[ResultSet])).thenReturn(userDetailRs)
    when(mockCs.getObject(8, classOf[ResultSet])).thenReturn(clientDetailRs)

    when(userInfoRs.next()).thenReturn(true)
    when(userAddrRs.next()).thenReturn(true)
    when(clientInfoRs.next()).thenReturn(true)
    when(clientAddrRs.next()).thenReturn(true)
    when(userDetailRs.next()).thenReturn(true)
    when(clientDetailRs.next()).thenReturn(true)

    stubTraderInfoRs(userInfoRs)
    stubAddrContactRs(userAddrRs)
    stubTraderDetailsRs(userDetailRs)
    stubTraderInfoRs(clientInfoRs, traderName = "Client Ltd")
    stubAddrContactRs(clientAddrRs)
    stubTraderDetailsRs(clientDetailRs)

    val result = repository.getTraderDetails("123456789", Some("987654321")).futureValue

    result                                 shouldBe defined
    result.get.userTrader.vrn              shouldBe "123456789"
    result.get.clientTrader                shouldBe defined
    result.get.clientTrader.get.vrn        shouldBe "987654321"
    result.get.clientTrader.get.traderName shouldBe Some("Client Ltd")

    verify(mockCs).setString(1, "123456789")
    verify(mockCs).setString(2, "987654321")
  }

  "getTraderDetails" should "return None when user info cursor is empty" in {
    val emptyRs = mock(classOf[ResultSet])
    when(mockCs.getObject(3, classOf[ResultSet])).thenReturn(emptyRs)
    when(mockCs.getObject(4, classOf[ResultSet])).thenReturn(emptyRs)
    when(mockCs.getObject(5, classOf[ResultSet])).thenReturn(emptyRs)
    when(mockCs.getObject(6, classOf[ResultSet])).thenReturn(emptyRs)
    when(mockCs.getObject(7, classOf[ResultSet])).thenReturn(emptyRs)
    when(mockCs.getObject(8, classOf[ResultSet])).thenReturn(emptyRs)
    when(emptyRs.next()).thenReturn(false)

    val result = repository.getTraderDetails("000000000", None).futureValue
    result shouldBe None
  }

  // ─── getClientListDownloadStatus ─────────────────────────────────────────────

  "getClientListDownloadStatus" should "return the status integer from the stored procedure" in {
    when(mockCs.getInt(4)).thenReturn(1)

    val result = repository.getClientListDownloadStatus("cred-123", "NOVA", 14400).futureValue

    result shouldBe 1
    verify(mockCs).setString(1, "cred-123")
    verify(mockCs).setString(2, "NOVA")
    verify(mockCs).setInt(3, 14400)
    verify(mockCs).registerOutParameter(4, OracleTypes.INTEGER)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  // ─── getAllClients ────────────────────────────────────────────────────────────

  "getAllClients" should "return a populated NovaClientListResponse" in {
    val clientListRs = mock(classOf[ResultSet])
    val clientNameCharsRs = mock(classOf[ResultSet])

    when(mockCs.getInt(6)).thenReturn(2)
    when(mockCs.getObject(7, classOf[ResultSet])).thenReturn(clientListRs)
    when(mockCs.getObject(8, classOf[ResultSet])).thenReturn(clientNameCharsRs)

    when(clientListRs.next()).thenReturn(true, true, false)
    when(clientListRs.getString("CLIENT_NAME")).thenReturn("Jones Motors Ltd", "Smith Supplies")
    when(clientListRs.getString("VAT_REG_NUMBER")).thenReturn("111222333", "444555666")

    when(clientNameCharsRs.next()).thenReturn(true, true, false)
    when(clientNameCharsRs.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("J", "S")

    val result = repository.getAllClients("cred-123", 0, -1, 0, "ASC").futureValue

    result.totalCount                         shouldBe 2
    result.clients                              should have size 2
    result.clients.head.name                  shouldBe "Jones Motors Ltd"
    result.clients.head.vatRegistrationNumber shouldBe "111222333"
    result.clientNameStartingCharacters       shouldBe List("J", "S")

    verify(mockCs).setString(1, "cred-123")
    verify(mockCs).setInt(2, 0)
    verify(mockCs).setInt(3, -1)
    verify(mockCs).setInt(4, 0)
    verify(mockCs).setString(5, "ASC")
    verify(mockCs).registerOutParameter(6, OracleTypes.INTEGER)
    verify(mockCs).registerOutParameter(7, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(8, OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  "getAllClients" should "return empty response when cursors are null" in {
    when(mockCs.getInt(6)).thenReturn(0)
    when(mockCs.getObject(7, classOf[ResultSet])).thenReturn(null)
    when(mockCs.getObject(8, classOf[ResultSet])).thenReturn(null)

    val result = repository.getAllClients("cred-123", 0, -1, 0, "ASC").futureValue

    result.totalCount                   shouldBe 0
    result.clients                      shouldBe List.empty
    result.clientNameStartingCharacters shouldBe List.empty
  }

  // ─── getClientsByVrn ─────────────────────────────────────────────────────────

  "getClientsByVrn" should "return a NovaClientListResponse matching the VRN" in {
    val clientListRs = mock(classOf[ResultSet])
    val clientNameCharsRs = mock(classOf[ResultSet])

    when(mockCs.getObject(3, classOf[ResultSet])).thenReturn(clientListRs)
    when(mockCs.getObject(4, classOf[ResultSet])).thenReturn(clientNameCharsRs)

    when(clientListRs.next()).thenReturn(true, false)
    when(clientListRs.getString("CLIENT_NAME")).thenReturn("Jones Motors Ltd")
    when(clientListRs.getString("VAT_REG_NUMBER")).thenReturn("111222333")

    when(clientNameCharsRs.next()).thenReturn(true, false)
    when(clientNameCharsRs.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("J")

    val result = repository.getClientsByVrn("cred-123", "111222333").futureValue

    result.clients                              should have size 1
    result.clients.head.vatRegistrationNumber shouldBe "111222333"
    result.totalCount                         shouldBe 1

    verify(mockCs).setString(1, "cred-123")
    verify(mockCs).setString(2, "111222333")
    verify(mockCs).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(4, OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  // ─── getClientsByName ────────────────────────────────────────────────────────

  "getClientsByName" should "return a NovaClientListResponse for a name search" in {
    val clientListRs = mock(classOf[ResultSet])
    val clientNameCharsRs = mock(classOf[ResultSet])

    when(mockCs.getInt(7)).thenReturn(1)
    when(mockCs.getObject(8, classOf[ResultSet])).thenReturn(clientListRs)
    when(mockCs.getObject(9, classOf[ResultSet])).thenReturn(clientNameCharsRs)

    when(clientListRs.next()).thenReturn(true, false)
    when(clientListRs.getString("CLIENT_NAME")).thenReturn("Jones Motors Ltd")
    when(clientListRs.getString("VAT_REG_NUMBER")).thenReturn("111222333")

    when(clientNameCharsRs.next()).thenReturn(true, false)
    when(clientNameCharsRs.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("J")

    val result = repository.getClientsByName("cred-123", "Jones Motors Ltd", 0, -1, 0, "ASC").futureValue

    result.totalCount        shouldBe 1
    result.clients.head.name shouldBe "Jones Motors Ltd"

    verify(mockCs).setString(1, "cred-123")
    verify(mockCs).setString(2, "Jones Motors Ltd")
    verify(mockCs).setInt(3, 0)
    verify(mockCs).setInt(4, -1)
    verify(mockCs).setInt(5, 0)
    verify(mockCs).setString(6, "ASC")
    verify(mockCs).registerOutParameter(7, OracleTypes.INTEGER)
    verify(mockCs).registerOutParameter(8, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(9, OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  // ─── getClientsByNameStart ───────────────────────────────────────────────────

  "getClientsByNameStart" should "return a NovaClientListResponse for a name-start search" in {
    val clientListRs = mock(classOf[ResultSet])
    val clientNameCharsRs = mock(classOf[ResultSet])

    when(mockCs.getInt(7)).thenReturn(2)
    when(mockCs.getObject(8, classOf[ResultSet])).thenReturn(clientListRs)
    when(mockCs.getObject(9, classOf[ResultSet])).thenReturn(clientNameCharsRs)

    when(clientListRs.next()).thenReturn(true, true, false)
    when(clientListRs.getString("CLIENT_NAME")).thenReturn("Jones Motors Ltd", "Jones Supplies")
    when(clientListRs.getString("VAT_REG_NUMBER")).thenReturn("111222333", "444555666")

    when(clientNameCharsRs.next()).thenReturn(true, false)
    when(clientNameCharsRs.getString("CLIENTNAMESTARTINGCHARACTER")).thenReturn("J")

    val result = repository.getClientsByNameStart("cred-123", "J", 0, -1, 0, "ASC").futureValue

    result.totalCount shouldBe 2
    result.clients      should have size 2

    verify(mockCs).setString(1, "cred-123")
    verify(mockCs).setString(2, "J")
    verify(mockCs).registerOutParameter(7, OracleTypes.INTEGER)
    verify(mockCs).registerOutParameter(8, OracleTypes.CURSOR)
    verify(mockCs).registerOutParameter(9, OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  // ─── hasClient ───────────────────────────────────────────────────────────────

  "hasClient" should "return true when stored procedure returns 1" in {
    when(mockCs.getInt(3)).thenReturn(1)

    val result = repository.hasClient("cred-123", "111222333").futureValue

    result shouldBe true
    verify(mockCs).setString(1, "cred-123")
    verify(mockCs).setString(2, "111222333")
    verify(mockCs).registerOutParameter(3, OracleTypes.INTEGER)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  "hasClient" should "return false when stored procedure returns 0" in {
    when(mockCs.getInt(3)).thenReturn(0)

    val result = repository.hasClient("cred-123", "999999999").futureValue

    result shouldBe false
  }

  // ─── getTraderInformation ────────────────────────────────────────────────────

  "getTraderInformation" should "return Some(TraderInformation) when the cursor has a row and gracePeriod is None" in {
    val traderRs = mock(classOf[ResultSet])

    when(mockCs.getObject(3, classOf[ResultSet])).thenReturn(traderRs)
    when(traderRs.next()).thenReturn(true)

    when(traderRs.getString("status")).thenReturn("REGD")
    when(traderRs.getString("trader_name")).thenReturn("ABC Ltd")
    when(traderRs.getString("trading_name")).thenReturn("ABC Trading")
    when(traderRs.getString("bus_address_1")).thenReturn("1 Test Street")
    when(traderRs.getString("bus_address_2")).thenReturn("Testville")
    when(traderRs.getString("bus_address_3")).thenReturn(null)
    when(traderRs.getString("bus_address_4")).thenReturn(null)
    when(traderRs.getString("bus_postcode")).thenReturn("SW1A 1AA")
    when(traderRs.getString("email")).thenReturn("abc@example.com")
    when(traderRs.getString("organisation_type")).thenReturn("LIMITED_COMPANY")
    when(traderRs.getString("trade_class")).thenReturn("47")
    when(traderRs.getString("trade_class_desc")).thenReturn("Retail trade")
    when(traderRs.getDate("effective_reg_date")).thenReturn(Date.valueOf(LocalDate.of(2000, 1, 1)))
    when(traderRs.getDate("ceased_date")).thenReturn(null)
    when(traderRs.getDate("cert_issued_date")).thenReturn(Date.valueOf(LocalDate.of(2000, 1, 10)))
    when(traderRs.getDate("next_return_pe_date")).thenReturn(Date.valueOf(LocalDate.of(2026, 3, 31)))
    when(traderRs.getString("return_stagger")).thenReturn("MAR")

    val result = repository.getTraderInformation("123456789", None).futureValue

    result                   shouldBe defined
    result.get.vrn           shouldBe "123456789"
    result.get.traderName    shouldBe Some("ABC Ltd")
    result.get.returnStagger shouldBe Some("MAR")

    verify(mockCs).setString(1, "123456789")
    verify(mockCs).setNull(2, Types.NUMERIC)
    verify(mockCs).registerOutParameter(3, OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  "getTraderInformation" should "pass the gracePeriod value when Some is provided" in {
    val traderRs = mock(classOf[ResultSet])

    when(mockCs.getObject(3, classOf[ResultSet])).thenReturn(traderRs)
    when(traderRs.next()).thenReturn(true)
    stubTraderInformationRs(traderRs)

    repository.getTraderInformation("123456789", Some(7200)).futureValue

    verify(mockCs).setString(1, "123456789")
    verify(mockCs).setInt(2, 7200)
  }

  "getTraderInformation" should "return None when the cursor is empty" in {
    val emptyRs = mock(classOf[ResultSet])
    when(mockCs.getObject(3, classOf[ResultSet])).thenReturn(emptyRs)
    when(emptyRs.next()).thenReturn(false)

    val result = repository.getTraderInformation("123456789", None).futureValue

    result shouldBe None
  }

  // ─── getVehicleStatusDetails ─────────────────────────────────────────────────

  "getVehicleStatusDetails" should "return Some(VehicleStatusDetails) with derived secured and imported booleans" in {
    val vehicleRs = mock(classOf[ResultSet])

    when(mockCs.getObject(2, classOf[ResultSet])).thenReturn(vehicleRs)
    when(vehicleRs.next()).thenReturn(true)
    when(vehicleRs.getString("p_vin")).thenReturn("WBA12345678901234")
    when(vehicleRs.getString("p_nova_ref")).thenReturn("NOVA26E100001")
    when(vehicleRs.getString("p_make")).thenReturn("BMW")
    when(vehicleRs.getString("p_model")).thenReturn("3 Series")
    when(vehicleRs.getObject("p_mileage")).thenReturn(Integer.valueOf(5000))
    when(vehicleRs.getInt("p_mileage")).thenReturn(5000)
    when(vehicleRs.getDate("p_first_reg_date")).thenReturn(Date.valueOf(LocalDate.of(2024, 1, 15)))
    when(vehicleRs.getString("p_status")).thenReturn("secured")
    when(vehicleRs.getDate("p_restriction_date")).thenReturn(null)
    when(vehicleRs.getString("p_origin")).thenReturn("IMPORT")

    val result = repository.getVehicleStatusDetails("WBA12345678901234").futureValue

    result              shouldBe defined
    result.get.vin      shouldBe "WBA12345678901234"
    result.get.novaRef  shouldBe Some("NOVA26E100001")
    result.get.make     shouldBe Some("BMW")
    result.get.mileage  shouldBe Some(5000)
    result.get.secured  shouldBe true
    result.get.imported shouldBe true

    verify(mockCs).setString(1, "WBA12345678901234")
    verify(mockCs).registerOutParameter(2, OracleTypes.CURSOR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  "getVehicleStatusDetails" should "return secured=false and imported=false when status and origin do not match" in {
    val vehicleRs = mock(classOf[ResultSet])

    when(mockCs.getObject(2, classOf[ResultSet])).thenReturn(vehicleRs)
    when(vehicleRs.next()).thenReturn(true)
    when(vehicleRs.getString("p_vin")).thenReturn("WBA12345678901234")
    when(vehicleRs.getString("p_nova_ref")).thenReturn(null)
    when(vehicleRs.getString("p_make")).thenReturn(null)
    when(vehicleRs.getString("p_model")).thenReturn(null)
    when(vehicleRs.getObject("p_mileage")).thenReturn(null)
    when(vehicleRs.getDate("p_first_reg_date")).thenReturn(null)
    when(vehicleRs.getString("p_status")).thenReturn("NOTIFIED")
    when(vehicleRs.getDate("p_restriction_date")).thenReturn(null)
    when(vehicleRs.getString("p_origin")).thenReturn("UK")

    val result = repository.getVehicleStatusDetails("WBA12345678901234").futureValue

    result.get.secured  shouldBe false
    result.get.imported shouldBe false
  }

  "getVehicleStatusDetails" should "return None when the cursor is empty" in {
    val emptyRs = mock(classOf[ResultSet])
    when(mockCs.getObject(2, classOf[ResultSet])).thenReturn(emptyRs)
    when(emptyRs.next()).thenReturn(false)

    val result = repository.getVehicleStatusDetails("UNKNOWN_VIN").futureValue

    result shouldBe None
  }

  // ─── getVehicleCalculationData ───────────────────────────────────────────────

  "getVehicleCalculationData" should "return VehicleCalculationData and hardcode toCurrency as GBP" in {
    when(mockCs.getBigDecimal(5)).thenReturn(new JBigDecimal("1.1523"))
    when(mockCs.getDate(6)).thenReturn(Date.valueOf(LocalDate.of(2011, 1, 4)))
    when(mockCs.getBigDecimal(7)).thenReturn(new JBigDecimal("20.0"))
    when(mockCs.getDate(8)).thenReturn(Date.valueOf(LocalDate.of(2012, 4, 1)))
    when(mockCs.getBigDecimal(9)).thenReturn(new JBigDecimal("50.00"))
    when(mockCs.getDate(10)).thenReturn(Date.valueOf(LocalDate.of(2012, 4, 1)))
    when(mockCs.getInt(11)).thenReturn(14)
    when(mockCs.wasNull()).thenReturn(false)
    when(mockCs.getDate(12)).thenReturn(Date.valueOf(LocalDate.of(2012, 4, 1)))
    when(mockCs.getBigDecimal(13)).thenReturn(new JBigDecimal("5.00"))
    when(mockCs.getDate(14)).thenReturn(Date.valueOf(LocalDate.of(2012, 4, 1)))
    when(mockCs.getInt(15)).thenReturn(100)
    when(mockCs.getDate(16)).thenReturn(Date.valueOf(LocalDate.of(2012, 4, 1)))
    when(mockCs.getBigDecimal(17)).thenReturn(new JBigDecimal("500.00"))

    val invoiceDate = LocalDate.of(2024, 1, 15)
    val arrivalDate = LocalDate.of(2024, 2, 1)

    val result = repository.getVehicleCalculationData("EUR", invoiceDate, arrivalDate).futureValue

    result.exchangeRate  shouldBe Some(BigDecimal("1.1523"))
    result.vatRate       shouldBe Some(BigDecimal("20.0"))
    result.thresholdDays shouldBe Some(14)
    result.maxNoOfDays   shouldBe Some(100)

    verify(mockCs).setString(1, "EUR")
    verify(mockCs).setString(2, "GBP")
    verify(mockCs).setDate(3, Date.valueOf(invoiceDate))
    verify(mockCs).setDate(4, Date.valueOf(arrivalDate))
    verify(mockCs).registerOutParameter(5, Types.NUMERIC)
    verify(mockCs).registerOutParameter(11, Types.INTEGER)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  // ─── getEuMemberStates ───────────────────────────────────────────────────────

  "getEuMemberStates" should "return only active EU members and Croatia, sorted by country code" in {
    val statesRs = mock(classOf[ResultSet])

    when(mockCs.getObject(1, classOf[ResultSet])).thenReturn(statesRs)
    // Four rows: DE (active), GB (left EU), HR (Croatia, no joining date), FR (active)
    when(statesRs.next()).thenReturn(true, true, true, true, false)
    when(statesRs.getString("p_country_code")).thenReturn("DE", "GB", "HR", "FR")
    when(statesRs.getString("p_coutry_desc")).thenReturn("Germany", "United Kingdom", "Croatia", "France")
    when(statesRs.getDate("p_eu_joining_date"))
      .thenReturn(
        Date.valueOf(LocalDate.of(1958, 1, 1)), // DE
        Date.valueOf(LocalDate.of(1973, 1, 1)), // GB
        null, // HR (no joining date)
        Date.valueOf(LocalDate.of(1958, 1, 1)) // FR
      )
    when(statesRs.getDate("p_eu_leaving_date"))
      .thenReturn(
        null, // DE (active)
        Date.valueOf(LocalDate.of(2020, 1, 31)), // GB (left)
        null, // HR
        null // FR (active)
      )
    when(statesRs.getDate("p_eu_accessionary_date")).thenReturn(null)

    val result = repository.getEuMemberStates().futureValue

    // GB excluded (has leaving date), HR included unconditionally, DE and FR included as active
    result.euMemberStates                      should have size 3
    result.euMemberStates.map(_.countryCode) shouldBe List("DE", "FR", "HR")
  }

  "getEuMemberStates" should "return empty list when cursor is null" in {
    when(mockCs.getObject(1, classOf[ResultSet])).thenReturn(null)

    val result = repository.getEuMemberStates().futureValue

    result shouldBe EuMemberStatesResponse(List.empty)
  }

  // ─── getNvraKnownFacts ───────────────────────────────────────────────────────

  "getNvraKnownFacts" should "return NvraKnownFacts with data when resultCode is 000" in {
    when(mockCs.getString(2)).thenReturn("NVRA123456")
    when(mockCs.getString(3)).thenReturn("Test Agent Ltd")
    when(mockCs.getString(4)).thenReturn("1 Agent Street")
    when(mockCs.getString(5)).thenReturn("Agentville")
    when(mockCs.getString(6)).thenReturn(null)
    when(mockCs.getString(7)).thenReturn(null)
    when(mockCs.getString(8)).thenReturn(null)
    when(mockCs.getString(9)).thenReturn("AG1 1NT")
    when(mockCs.getString(10)).thenReturn("N")
    when(mockCs.getString(11)).thenReturn("000")

    val result = repository.getNvraKnownFacts("NVRA123456").futureValue

    result.nvraRefNumber shouldBe "NVRA123456"
    result.agentName     shouldBe Some("Test Agent Ltd")
    result.addressLine1  shouldBe Some("1 Agent Street")
    result.addressLine3  shouldBe None
    result.resultCode    shouldBe "000"

    verify(mockCs).setString(1, "NVRA123456")
    verify(mockCs).registerOutParameter(2, Types.VARCHAR)
    verify(mockCs).registerOutParameter(11, Types.VARCHAR)
    verify(mockCs).execute()
    verify(mockCs).close()
  }

  "getNvraKnownFacts" should "return NvraKnownFacts with resultCode 001 when no record found" in {
    when(mockCs.getString(2)).thenReturn("NVRA999999")
    when(mockCs.getString(3)).thenReturn(null)
    when(mockCs.getString(4)).thenReturn(null)
    when(mockCs.getString(5)).thenReturn(null)
    when(mockCs.getString(6)).thenReturn(null)
    when(mockCs.getString(7)).thenReturn(null)
    when(mockCs.getString(8)).thenReturn(null)
    when(mockCs.getString(9)).thenReturn(null)
    when(mockCs.getString(10)).thenReturn(null)
    when(mockCs.getString(11)).thenReturn("001")

    val result = repository.getNvraKnownFacts("NVRA999999").futureValue

    result.resultCode shouldBe "001"
    result.agentName  shouldBe None
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────────

  private def stubTraderInfoRs(rs: ResultSet, traderName: String = "Test Trader Ltd"): Unit = {
    when(rs.getString("STATUS")).thenReturn("REGD")
    when(rs.getString("TRADER_NAME")).thenReturn(traderName)
    when(rs.getString("TRADING_NAME")).thenReturn("Test Trader")
    when(rs.getString("BUS_ADDRESS_1")).thenReturn("1 Test Street")
    when(rs.getString("BUS_ADDRESS_2")).thenReturn("Test Town")
    when(rs.getString("BUS_ADDRESS_3")).thenReturn(null)
    when(rs.getString("BUS_ADDRESS_4")).thenReturn(null)
    when(rs.getString("BUS_POSTCODE")).thenReturn("TE1 1ST")
    when(rs.getString("EMAIL")).thenReturn("test@test.com")
    when(rs.getString("TRADE_CLASS")).thenReturn("47")
    when(rs.getString("TRADE_CLASS_DESC")).thenReturn("Retail trade")
    when(rs.getString("ORGANISATION_TYPE")).thenReturn("LIMITED_COMPANY")
    when(rs.getDate("EFFECTIVE_REG_DATE")).thenReturn(Date.valueOf(LocalDate.of(2000, 1, 1)))
    when(rs.getDate("CEASED_DATE")).thenReturn(null)
    when(rs.getDate("CERT_ISSUED_DATE")).thenReturn(Date.valueOf(LocalDate.of(2000, 1, 10)))
    when(rs.getDate("NEXT_RETURN_PE_DATE")).thenReturn(Date.valueOf(LocalDate.of(2026, 3, 31)))
    when(rs.getString("RETURN_STAGGER")).thenReturn("MAR")
  }

  private def stubAddrContactRs(rs: ResultSet): Unit = {
    when(rs.getString("REDUNDANT_TRADER")).thenReturn("N")
    when(rs.getInt("INSOLVENCY_STATUS")).thenReturn(0)
    when(rs.getString("DAYTIME_PHONE")).thenReturn("01234567890")
    when(rs.getString("MOBILE_PHONE")).thenReturn(null)
  }

  private def stubTraderDetailsRs(rs: ResultSet): Unit = {
    when(rs.getString("MISSING_TRADER_IND")).thenReturn("N")
  }

  private def stubTraderInformationRs(rs: ResultSet): Unit = {
    when(rs.getString("status")).thenReturn("REGD")
    when(rs.getString("trader_name")).thenReturn("ABC Ltd")
    when(rs.getString("trading_name")).thenReturn("ABC Trading")
    when(rs.getString("bus_address_1")).thenReturn("1 Test Street")
    when(rs.getString("bus_address_2")).thenReturn("Testville")
    when(rs.getString("bus_address_3")).thenReturn(null)
    when(rs.getString("bus_address_4")).thenReturn(null)
    when(rs.getString("bus_postcode")).thenReturn("SW1A 1AA")
    when(rs.getString("email")).thenReturn("abc@example.com")
    when(rs.getString("organisation_type")).thenReturn("LIMITED_COMPANY")
    when(rs.getString("trade_class")).thenReturn("47")
    when(rs.getString("trade_class_desc")).thenReturn("Retail trade")
    when(rs.getDate("effective_reg_date")).thenReturn(Date.valueOf(LocalDate.of(2000, 1, 1)))
    when(rs.getDate("ceased_date")).thenReturn(null)
    when(rs.getDate("cert_issued_date")).thenReturn(Date.valueOf(LocalDate.of(2000, 1, 10)))
    when(rs.getDate("next_return_pe_date")).thenReturn(Date.valueOf(LocalDate.of(2026, 3, 31)))
    when(rs.getString("return_stagger")).thenReturn("MAR")
  }
}
