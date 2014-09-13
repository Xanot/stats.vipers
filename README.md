# Dev prerequisites

  * JDK
  * SBT (http://www.scala-sbt.org/)
  * NodeJs (http://nodejs.org/)
  * Bower: Execute "npm install -g bower"
  * Gulp: Execute "npm install -g gulp"

Note: IDEA Community Edition(with the Scala plugin) / WebStorm combo is recommended. You may generate the IntelliJ project file by executing "gen-idea" in sbt.

# Configuration

You may set the SOE service id in fetcher.conf. The default one throttles the number of requests.

# Dependencies

The following sub-projects need to be served locally:

   * "common" -> Execute "common/publishLocal" using sbt
   * "fetcher" -> Execute "fetcher/publishLocal"
   * "indexer" -> Execute "indexer/publishLocal"

Note: Don't forget to publish it again whenever you make any changes to a sub-project

Client dependencies can be installed by executing "npm install && bower install" inside /web/src/main/webapp 

# Database configuration

Set the dbms you want to use along with its url and optional user/password in indexer/src/main/resources/database.conf

# How to test-run

Execute "gulp build" inside /web/src/main/webapp. This generates a "client" folder inside /web/src/main/resources containing the minified client and its libraries

Execute "web/run" in sbt

Navigate to http://localhost:8080/ui/index.html

# How to run tests

Each sub-project has its own set of tests(e.g. if you want to run the "fetcher" tests, execute "fetcher/test" in sbt)

You may run the client tests by executing "gulp test" inside /web/src/main/webapp

# Deployment instructions

TODO