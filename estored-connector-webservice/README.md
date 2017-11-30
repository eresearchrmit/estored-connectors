eStoRED Web Service Connector
=============================

This is a data source of the eStoRED platform (http://bitbucket.org/eresearchrmit/estored). It connects to a RabbitMQ broker (http://www.rabbitmq.com/) - which is at the center of the eStoRED architecture - using configuration variables, and listens to specific commands.

When receiving a message from the RabbitMQ broker, it handles it by retrieving the URL parameter in the message header, connecting to that URL and retrieving the content of the required URL, and sends a message back to the same RabbitMQ broker with this data.

Installation
============

From the sources
----------------

1. Retrieve the project sources from Bitbucket repository: https://bitbucket.org/eresearchrmit/estored-connector-webservice

2. Install Maven : https://maven.apache.org/install.html

3. Package the sources into a JAR file by running the following command in your terminal:

	```
	mvn clean package
	```

4. Run the "*main*" method from "*edu.rmit.eres.estored.connectors.WebServiceConnnector*" class to start the program

Using Docker
------------

To set up using Docker, deploy this program as a container as part of the eStoRED platform.

To do so, follow the instructions from the Bitbucket repository: https://bitbucket.org/eresearchrmit/docker-estored

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
# Set to true in case the RabbitMQ Broker uses a self signed certificate to automatically trust
SSL_AUTOTRUST_SELFSIGNED=true

Start JAR file.

License
=======

eStoRED and its connectors are distributed under the 3-clause "New" BSD License. See [License.txt](https://bitbucket.org/eresearchrmit/estored-connector-webservice/src/master/LICENSE.txt) file.