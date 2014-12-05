## Dev prerequisites

  * SBT (http://www.scala-sbt.org/)
  * NodeJs (http://nodejs.org/)
  * Bower: Execute "npm install -g bower"
  * Gulp: Execute "npm install -g gulp"

## Configuration

- You may set the SOE service id in fetcher.conf. The default one throttles the number of requests.

## Dependencies

- Client dependencies can be installed by executing "npm install && bower install" inside /web/src/main/webapp 

## Database configuration

- The default dbms is H2 
- You may set the dbms you want to use along with its url and optional user/password in indexer/src/main/resources/database.conf


## How to test-run

- Execute "gulp build" inside /web/src/main/webapp
- Execute "web/run" in sbt
- Navigate to http://localhost:8080

## How to run the tests

- Each sub-project has its own set of tests(e.g. if you want to run the "fetcher" tests, execute "fetcher/test" in sbt)
- You may run the client tests by executing "gulp test" inside /web/src/main/webapp

## Deployment instructions

TODO