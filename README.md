# Commit Trace Oracle

## Prerequisites
Following tools and frameworks are required for running the project:

* [Apache Maven 3.5+](https://maven.apache.org/)
* [Git](https://git-scm.com/)
* [Docker](https://www.docker.com/)
* [Java 8](https://www.oracle.com/java/technologies/java8.html)
* [Spring Framework](https://spring.io/)
* [MongoDB 5.0](https://www.mongodb.com)
* [InfluxDB 2.7.4](https://www.influxdata.com/)
* [Grafana](https://grafana.com/)

### Configuration

#### Git
* To ignore local.yml file change execute command 'git update-index --assume-unchanged .\src\main\resources\local.yml'
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)

### Docker Compose support

This project contains a Docker Compose file named `compose.yaml`.
In this file, the following services have been defined:

* mongodb: [`mongo:latest`](https://hub.docker.com/_/mongo)

Please review the tags of the used images and set them to the same as you're running in production.
### Running from command line prompt
Commit Trace Detail:
-command commit-trace-detail -tracer-name historyFinder -cache-directory ../academic -repository-url https://github.com/checkstyle/checkstyle.git -start-commit 119fd4fb33bef9f5c66fc950396669af842c21a3 -file src/main/java/com/puppycrawl/tools/checkstyle/Checker.java -element-name fireErrors -start-line 384 -output-file ../repertory/trace-detail.json

Commit Trace Shaw:
-command commit-trace-shaw -cache-directory ../academic -repository-url https://github.com/checkstyle/checkstyle.git -start-commit 119fd4fb33bef9f5c66fc950396669af842c21a3 -file src/main/java/com/puppycrawl/tools/checkstyle/Checker.java -element-name fireErrors -start-line 384 -output-file ../repertory/commit-trace-shaw.csv