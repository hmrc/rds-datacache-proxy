
# rds-datacache-proxy

RDS Data Cache Proxy repo for NDDS, CIS and Machine Games Duty (MGD) connects with RDS Oracle database.

## Developer setup
[Developer setup](https://confluence.tools.tax.service.gov.uk/display/RBD/Local+Machine+Setup+to+run+and+connect+to+Oracle+database)


## Running the service

Service Manager for NDDS: `sm2 --start NDDS_ALL`

To check libraries update, run all tests and coverage: `./run_all_tests.sh`

To start the server locally: `sbt 'run 6992'` or `sbt run`

To execute the scala formatter: `./run_all_checks.sh`

### Machine Games Duty (MGD)

#### Overview

The MGD module provides a Return Summary endpoint which retrieves:

Number of returns due
Number of returns overdue

Data is sourced via Oracle stored procedure:

`MGD_DC_RTN_PCK.GET_RETURN_SUMMARY`

#### Endpoint

`GET /gambling/return-summary/:mgdRegNumber`

#### Example

`curl http://localhost:6992/rds-datacache-proxy/gambling/return-summary/XYZ00000000001`

#### Sample Response

```
{
  "mgdRegNumber": "XYZ00000000001",
  "returnsDue": 0,
  "returnsOverdue": 1
}
```

#### Validation Rules

MGD Registration Number must match regular expression:`^[A-Z]{3}[0-9]{11}$`

#### MGD Certificate Endpoint

The MGD module provides a Certificate endpoint which retrieves registration certificate details including business information, address, partners, group members, and return periods.

Data is sourced via Oracle stored procedure:

`MGD_DC_CERT_PCK.GET_MGD_CERTIFICATE`

#### Endpoint

`GET /gambling/mgd-certificate/:mgdRegNumber`

#### Example

`curl http://localhost:6992/rds-datacache-proxy/gambling/mgd-certificate/XYZ00000000001`

#### Sample Response
```
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


#### Operator Details Endpoint

The MGD module provides an Operator Details endpoint which retrieves operator information including trading name, business name, and address details.

Data is sourced via Oracle stored procedure:

`MGD_DC_OPR_PCK.GET_OPERATOR_DETAILS`

#### Endpoint

`GET /gambling/operator-details/:mgdRegNumber`

#### Example

`curl http://localhost:6992/rds-datacache-proxy/gambling/operator-details/XYZ00000000001`

#### Sample Response

```
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



#### Business Details Endpoint

The MGD module provides a Business Details endpoint which retrieves registration and business structure information.

Data is sourced via Oracle stored procedure:

`MGD_DC_BUS_PCK.GET_BUSINESS_DETAILS`

#### Endpoint

`GET /gambling/business-details/:mgdRegNumber`

#### Example

`curl http://localhost:6992/rds-datacache-proxy/gambling/business-details/XYZ00000000001`

#### Sample Response


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
