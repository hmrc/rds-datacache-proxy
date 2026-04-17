
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


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
