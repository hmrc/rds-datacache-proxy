
# rds-datacache-proxy

RDS Data Cache Proxy repo for NDDS and CIS connects with RDS Oracle database.

## Developer setup
[Developer setup](https://confluence.tools.tax.service.gov.uk/display/RBD/Local+Machine+Setup+to+run+and+connect+to+Oracle+database)


## Running the service

Service Manager for NDDS: `sm2 --start NDDS_ALL`

To check libraries update, run all tests and coverage: `./run_all_tests.sh`

To start the server locally: `sbt 'run 6992'` or `sbt run`

To execute the scala formatter: `./run_all_checks.sh`


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
