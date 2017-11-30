eStoRED Examples Datasource
===========================

This is a data source of the eStoRED platform (http://bitbucket.org/eresearchrmit/estored). It connects to a RabbitMQ broker (http://www.rabbitmq.com/) - which is at the center of the eStoRED architecture - using configuration variables, and listens to specific commands.

When receiving a message from the RabbitMQ broker, it handles it by extracting the original routing key return example data if it matches one of the keywords for one of the examples, and sending back the data for that particular example to the RabbitMQ broker.

The following keywords are available:
- plain: simple "Hello, World!" text format
- csv: simple CSV text format, from a local CSV file
- numbers: 10 numbers in JSON format
- coordinates: 10 X and Y coordinates in JSON format
- map: 3 different map features (point, polyline, polygon) in GeoJSON format, from a local GeoJSON file
- laneways: laneways of Melbourne city as map features in GeoJSON format, from an online web service
- graph: graph in Neo4J JSON format, from an online web service
- biggraph: graph of researchers, publications and institutions in Neo4J JSON format, from a local JSON file 
- festostructure: graph of the topology of a production line in BeSpaceD JSON format, from a local JSON file
- festosensors: sensor information to be used with 'festostructure' keyword, in BeSpaceD JSON format, from a local JSON file

Installation
============

From the sources
----------------

1. Retrieve the project sources from Bitbucket repository: https://bitbucket.org/eresearchrmit/estored-examples-datasource

2. Install Maven : https://maven.apache.org/install.html

3. Package the sources into a JAR file by running the following command in your terminal:

	```
	mvn clean package
	```

4. Run the "*main*" method from "*edu.rmit.eres.estored.datasource.examples.ExamplesDatasource*" class to start the program

Using Docker
------------

To set up using Docker, deploy this program as a container as part of the eStoRED platform.

To do so, follow the instructions from the Bitbucket repository: https://bitbucket.org/eresearchrmit/docker-estored

Made connection details configurable from environment variables.

Usage
=====

Set the connection information as environment variables:

# The address of the the RabbitMQ broker
RABBITMQ_BROKER_ADDRESS=0.0.0.0
# RabbitMQ broker SSL port
RABBITMQ_BROKER_PORT=5671
# The virtual host to connect to within RabbitMQ
RABBITMQ_VHOST=/estored
# Username and password of the RabbitMQ user having READ and WRITE permissions on the Virtual Host
RABBITMQ_USER_NAME=username
RABBITMQ_USER_PASSWORD=password

Start JAR file.

License
=======

eStoRED is distributed under the 3-clause "New" BSD License. See [License.txt](https://bitbucket.org/eresearchrmit/estored-examples-datasource/src/master/LICENSE.txt) file.