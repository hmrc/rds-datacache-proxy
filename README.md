rds-datacache-proxy
===================

![](https://img.shields.io/github/v/release/hmrc/rds-datacache-proxy)

RDS Data Cache Proxy microservice connecting NDDS, CIS, and Machine Games Duty (MGD) to an RDS Oracle database.

The service is responsible for:

* Serving data for CIS premises and contractor details
* Serving MGD return summaries, certificates, operator details, business details, and partner details
* Serving Corporation Tax penalty transaction data
* Routing all queries through Oracle stored procedures

## Running the service

Start the full service profile using Service Manager:

```bash
sm2 --start NDDS_ALL
```

To run the service locally (default port **6992**):

```bash
sbt 'run 6992'
```

### Developer setup

See [Local Machine Setup to run and connect to Oracle database](https://confluence.tools.tax.service.gov.uk/display/RBD/Local+Machine+Setup+to+run+and+connect+to+Oracle+database) on Confluence.

## Testing

To run all tests and generate a coverage report:

```bash
./run_all_tests.sh
```

Or run unit and integration tests individually with SBT:

```bash
sbt clean compile test it/test
```

To execute the Scala formatter and run all checks:

```bash
./run_all_checks.sh
```

## Endpoints

### Construction Industry Scheme (CIS)

All CIS routes are prefixed with `/rds-datacache-proxy/cis`.

#### Get CIS Taxpayer by Tax Reference

Retrieves a CIS taxpayer record by employer reference.

```
POST /rds-datacache-proxy/cis
```

Request body:

```json
{
  "taxOfficeNumber": "123",
  "taxOfficeReference": "AB00000"
}
```

Sample response:

```json
{
  "uniqueId": "abc123",
  "taxOfficeNumber": "123",
  "taxOfficeRef": "AB00000",
  "aoDistrict": "01",
  "aoPayType": "P",
  "aoCheckCode": "A",
  "aoReference": "1234567890",
  "validBusinessAddr": "Y",
  "correlation": null,
  "ggAgentId": null,
  "employerName1": "Acme Contractors Ltd",
  "employerName2": null,
  "agentOwnRef": null,
  "schemeName": "Acme CIS Scheme",
  "utr": "1234567890",
  "enrolledSig": "Y"
}
```

#### Get Client List Download Status

Returns the current download status of an agent's client list.

```
GET /rds-datacache-proxy/cis/client-list-status?credentialId=&serviceName=&gracePeriod=
```

| Query parameter | Required | Default | Description |
|-----------------|----------|---------|-------------|
| `credentialId`  | Yes      |         | The agent's credential ID |
| `serviceName`   | Yes      |         | The service name |
| `gracePeriod`   | No       | `14400` | Grace period in seconds |

Sample response:

```json
{
  "status": "Succeeded"
}
```

Status values: `InitiateDownload`, `InProgress`, `Succeeded`, `Failed`

#### Get Client List

Retrieves a paginated, sortable list of clients for an agent.

```
GET /rds-datacache-proxy/cis/client-list?irAgentId=&credentialId=&start=&count=&sort=&ascending=
```

| Query parameter | Required | Default | Description |
|-----------------|----------|---------|-------------|
| `irAgentId`     | Yes      |         | The IR agent ID |
| `credentialId`  | Yes      |         | The agent's credential ID |
| `start`         | No       | `0`     | Pagination offset |
| `count`         | No       | `-1`    | Number of results (`-1` for all) |
| `sort`          | No       | `0`     | Sort column index |
| `ascending`     | No       | `true`  | Sort direction |

Sample response:

```json
{
  "clients": [
    {
      "uniqueId": "abc123",
      "taxOfficeNumber": "123",
      "taxOfficeRef": "AB00000",
      "aoDistrict": "01",
      "aoPayType": "P",
      "aoCheckCode": "A",
      "aoReference": "1234567890",
      "validBusinessAddr": "Y",
      "correlation": null,
      "ggAgentId": null,
      "employerName1": "Acme Contractors Ltd",
      "employerName2": null,
      "agentOwnRef": null,
      "schemeName": "Acme CIS Scheme"
    }
  ],
  "totalCount": 1,
  "clientNameStartingCharacters": ["A"]
}
```

#### Has Client

Checks whether a specific client exists for an agent.

```
GET /rds-datacache-proxy/cis/has-client?irAgentId=&credentialId=&taxOfficeNumber=&taxOfficeReference=
```

| Query parameter      | Required | Description |
|----------------------|----------|-------------|
| `irAgentId`          | Yes      | The IR agent ID |
| `credentialId`       | Yes      | The agent's credential ID |
| `taxOfficeNumber`    | Yes      | The client's tax office number |
| `taxOfficeReference` | Yes      | The client's tax office reference |

Sample response:

```json
{
  "hasClient": true
}
```

#### Get Contractor Prepop Data

Retrieves pre-population data for a contractor scheme using known facts.

```
POST /rds-datacache-proxy/cis/prepop-contractor
```

Request body:

```json
{
  "taxOfficeNumber": "123",
  "taxOfficeReference": "AB00000",
  "accountOfficeReference": "123PA0000000000"
}
```

Sample response:

```json
{
  "knownfacts": {
    "taxOfficeNumber": "123",
    "taxOfficeReference": "AB00000",
    "accountOfficeReference": "123PA0000000000"
  },
  "prePopContractor": {
    "schemeName": "Acme CIS Scheme",
    "utr": "1234567890",
    "response": 0
  }
}
```

#### Get Subcontractors Prepop Data

Retrieves pre-population data for subcontractors associated with a scheme using known facts.

```
POST /rds-datacache-proxy/cis/prepop-subcontractor
```

Request body:

```json
{
  "taxOfficeNumber": "123",
  "taxOfficeReference": "AB00000",
  "accountOfficeReference": "123PA0000000000"
}
```

Sample response:

```json
{
  "knownfacts": {
    "taxOfficeNumber": "123",
    "taxOfficeReference": "AB00000",
    "accountOfficeReference": "123PA0000000000"
  },
  "prePopSubcontractors": {
    "response": 0,
    "subcontractors": [
      {
        "subcontractorType": "Individual",
        "utr": "9876543210",
        "verificationNumber": "V123456",
        "verificationSuffix": "A",
        "title": "Mr",
        "firstName": "John",
        "secondName": "",
        "surname": "Smith"
      }
    ]
  }
}
```

#### Enqueue Message

Places a message onto a UDAS queue.

```
POST /rds-datacache-proxy/cis/enqueue-message
```

Request body:

```json
{
  "message": {
    "sender": "cis-service",
    "queueName": "CIS_QUEUE",
    "replyQueue": "CIS_REPLY_QUEUE",
    "correlationID": "abc-123",
    "filter": "CIS_FILTER",
    "payload": {
      "key": "value"
    }
  }
}
```

Sample response:

```json
{
  "messageIDOut": "msg-id-001"
}
```

### Machine Games Duty (MGD)

#### Return Summary

Retrieves the number of returns due and overdue.

Oracle stored procedure: `MGD_DC_RTN_PCK.GET_RETURN_SUMMARY`

```
GET /gambling/return-summary/:mgdRegNumber
```

Example:

```bash
curl http://localhost:6992/rds-datacache-proxy/gambling/return-summary/XYZ00000000001
```

Sample response:

```json
{
  "mgdRegNumber": "XYZ00000000001",
  "returnsDue": 0,
  "returnsOverdue": 1
}
```

Validation: MGD Registration Number must match `^[A-Z]{3}[0-9]{11}$`

#### Certificate

Retrieves registration certificate details including business information, address, partners, group members, and return periods.

Oracle stored procedure: `MGD_DC_CERT_PCK.GET_MGD_CERTIFICATE`

```
GET /gambling/mgd-certificate/:mgdRegNumber
```

Example:

```bash
curl http://localhost:6992/rds-datacache-proxy/gambling/mgd-certificate/XYZ00000000001
```

Sample response:

```json
{
  "mgdRegNumber": "XYZ00000000001",
  "registrationDate": "2024-04-29",
  "individualName": "John Smith",
  "businessName": "Test Business Ltd",
  "tradingName": "Test Trading",
  "repMemName": null,
  "busAddrLine1": "Line 1",
  "busAddrLine2": null,
  "busAddrLine3": null,
  "busAddrLine4": null,
  "busPostcode": "AB1 2CD",
  "busCountry": "UK",
  "busAdi": null,
  "repMemLine1": null,
  "repMemLine2": null,
  "repMemLine3": null,
  "repMemLine4": null,
  "repMemPostcode": null,
  "repMemAdi": null,
  "typeOfBusiness": "Gambling",
  "businessTradeClass": 1,
  "noOfPartners": 2,
  "groupReg": "N",
  "noOfGroupMems": 0,
  "dateCertIssued": "2026-04-29",
  "partMembers": [
    {
      "namesOfPartMems": "John Smith",
      "solePropTitle": "Mr",
      "solePropFirstName": "John",
      "solePropMiddleName": null,
      "solePropLastName": "Smith",
      "typeOfBusiness": 1
    }
  ],
  "groupMembers": [
    {
      "namesOfGroupMems": "Group Member Ltd"
    }
  ],
  "returnPeriodEndDates": [
    {
      "returnPeriodEndDate": "2025-03-31"
    }
  ]
}
```

#### Operator Details

Retrieves operator information including trading name, business name, and address details.

Oracle stored procedure: `MGD_DC_OPR_PCK.GET_OPERATOR_DETAILS`

```
GET /gambling/operator-details/:mgdRegNumber
```

Example:

```bash
curl http://localhost:6992/rds-datacache-proxy/gambling/operator-details/XYZ00000000001
```

Sample response:

```json
{
  "mgdRegNumber": "XYZ00000000001",
  "solePropName": "John Smith",
  "solePropTitle": "Mr",
  "solePropFirstName": "John",
  "solePropLastName": "Smith",
  "tradingName": "Test Trading",
  "businessName": "Test Business Ltd",
  "postcode": "AB1 2CD",
  "country": "UK"
}
```

#### Business Details

Retrieves registration and business structure information.

Oracle stored procedure: `MGD_DC_BUS_PCK.GET_BUSINESS_DETAILS`

```
GET /gambling/business-details/:mgdRegNumber
```

Example:

```bash
curl http://localhost:6992/rds-datacache-proxy/gambling/business-details/XYZ00000000001
```

#### Partner Details

Retrieves information about Business Partners.

```
GET /gambling/partner-details/MGD/:mgdRegNumber
```

Example:

```bash
curl http://localhost:6992/rds-datacache-proxy/gambling/partner-details/MGD/XYZ00000000001
```

Sample response:

```json
{
  "partners": [
    {
      "mgdRegNumber": "XCM00000000774",
      "dateOfJoining": "2013-08-01",
      "dateOfLeaving": "1999-12-31",
      "businessName": "Adams",
      "tradingName": "Adams",
      "utr": "1987650051",
      "address1": "2 Seaview Rd",
      "address2": "Milton Keynes",
      "postcode": "MK8 9DD",
      "iomOrCiFlag": "false",
      "phoneNumber": "08589876543",
      "isFutureLeaveDate": 0,
      "isFutureJoinDate": 0,
      "businessType": 4
    },
    {
      "mgdRegNumber": "XCM00000000774",
      "dateOfJoining": "2013-08-01",
      "dateOfLeaving": "1999-12-31",
      "businessName": "Adams",
      "tradingName": "Adams",
      "utr": "1987650051",
      "address1": "3 Seaview Rd",
      "address2": "Milton Keynes",
      "postcode": "MK8 8DR",
      "iomOrCiFlag": "false",
      "phoneNumber": "01511234567",
      "isFutureLeaveDate": 0,
      "isFutureJoinDate": 0,
      "businessType": 4
    }
  ],
  "systemDate": "2026-07-30"
}
```

### Corporation Tax

```
GET /corporation-tax/penalty-transactions/:identifier/:count
```

Example:

```bash
curl http://localhost:6992/rds-datacache-proxy/corporation-tax/penalty-transactions/1543423743/3
```

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
