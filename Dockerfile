# DOCKER-VERSION 1.8.1
FROM       ubuntu:14.04
MAINTAINER Jason Brown "jbrown@unicon.net"

# make sure the package repository is up to date
RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" > /etc/apt/sources.list
RUN apt-get -y update

# install python-software-properties (so you can do add-apt-repository)
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y -q python-software-properties software-properties-common

# Install Java.
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Add $HOME/.kettle/shared.xml
RUN mkdir $HOME/.kettle/
RUN echo '<?xml version="1.0" encoding="UTF-8"?>\
<sharedobjects>\
  <connection>\
    <name>LAP_connect</name>\
    <server/>\
    <type>H2</type>\
    <access>Native</access>\
    <database>mem&#x3a;temp-db&#x3b;MODE&#x3d;MYSQL&#x3b;DB_CLOSE_DELAY&#x3d;-1</database>\
    <port>-1</port>\
    <username>sa</username>\
    <password>Encrypted </password>\
    <servername/>\
    <data_tablespace/>\
    <index_tablespace/>\
    <attributes>\
      <attribute><code>PORT_NUMBER</code><attribute>-1</attribute></attribute>\
    </attributes>\
  </connection>\
</sharedobjects>' >> $HOME/.kettle/shared.xml

RUN mkdir opt/lap/

EXPOSE 8080

# java.security.edge reduces start up time due to 
# INFO: Creation of SecureRandom instance for session ID generation using [SHA1PRNG] took [5172] milliseconds.

WORKDIR /opt/lap/
CMD java -server -jar -Djava.security.egd=file:/dev/./urandom -Dspring.config.location=/opt/lap/dev.properties target/lap-1.0-SNAPSHOT.jar 