#!/usr/bin/env bash

set -u

CATALINA_HOME=$(catalina  | grep CATALINA_HOME | awk '{ print $NF }')

# 2.2.1. Configuring Tomcat 7/Tomcat 8
# 1. Create a folder named endorsed in $TOMCAT_HOME.
# 2. Create a folder named shared in $TOMCAT_HOME.
mkdir -p $CATALINA_HOME/{endorsed,shared/lib}

# 3. Edit the file $TOMCAT_HOME\conf\catalina.properties
# and change the property shared.loader so that it reads:
# shared.loader=${catalina.home}\shared\lib\*.jar.
# Note that for Linux OS it should be
# shared.loader=${catalina.home}/shared/lib/*.jar.
sed -i % 's|shared.loader=.*|shared.loader=${catalina.home}/shared/lib/*.jar|' $CATALINA_HOME/conf/catalina.properties
rm $CATALINA_HOME/conf/catalina.properties%

# Extract from the binary zip file (under AdditionalFiles\endorsed)
# the following libs to $TOMCAT_HOME\shared\lib:
cp AdditionalFiles/endorsed/*.jar $CATALINA_HOME/shared/lib

# Build and deploy the SP
mvn --file EIDAS-SP clean package -P embedded -P coreDependencies
cp EIDAS-SP/target/SP.war $CATALINA_HOME/webapps

# Restart Tomcat
catalina stop
catalina start
