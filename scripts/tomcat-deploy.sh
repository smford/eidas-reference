#!/usr/bin/env bash

project_root=$(dirname "$0")/..

# ---------------------------
# Tomcat setup
# ---------------------------

if hash catalina 2>/dev/null; then
  CATALINA_HOME=$(catalina  | grep CATALINA_HOME | awk '{ print $NF }')
  if [ ! -d "$CATALINA_HOME" ]; then
    >&2 echo "Error: Value for CATALINA_HOME ('$CATALINA_HOME') does not point to a directory."
    >&2 echo "Is your tomcat installation setup correctly?"
    exit 1
  fi
else
  >&2 echo "Error: Could not find a 'catalina' executable on the PATH. Do you have tomcat installed?"
  exit 1
fi

# 2.2.1. Configuring Tomcat 7/Tomcat 8
# 1. Create a folder named endorsed in $TOMCAT_HOME.
# 2. Create a folder named shared in $TOMCAT_HOME.
mkdir -p "$CATALINA_HOME"/{endorsed,shared/lib}

# 3. Edit the file $TOMCAT_HOME\conf\catalina.properties
# and change the property shared.loader so that it reads:
# shared.loader=${catalina.home}\shared\lib\*.jar.
# Note that for Linux OS it should be
# shared.loader=${catalina.home}/shared/lib/*.jar.

# shellcheck disable=SC2016
sed -i % 's|shared.loader=.*|shared.loader=${catalina.home}/shared/lib/*.jar|' "$CATALINA_HOME/conf/catalina.properties"
rm "$CATALINA_HOME/conf/catalina.properties%"

# Extract from the binary zip file (under AdditionalFiles\endorsed)
# the following libs to $TOMCAT_HOME\shared\lib:

# TODO: consider making these jar files properly declared dependencies in pom.xml (if they're published to maven central)
cp "$project_root"/AdditionalFiles/endorsed/*.jar "$CATALINA_HOME/shared/lib"

# ---------------------------
# Rebuild Everything
# ---------------------------
mvn --file EIDAS-Parent clean install -P embedded -P coreDependencies -Dmaven.test.skip=true

# ---------------------------
# Deploy the Service Provider
# ---------------------------

# Deploy the SP
cp "$project_root"/EIDAS-SP/target/SP.war "$CATALINA_HOME/webapps"

# ---------------------------
# Deploy the Connector Node
# ---------------------------

export EIDAS_CONFIG_REPOSITORY="$project_root"/EIDAS-Config/

# Deploy the Node
cp "$project_root"/EIDAS-Node/target/EidasNode.war "$CATALINA_HOME/webapps"

# ---------------------------
# Deploy the IdP
# ---------------------------

# Deploy the IdP
cp "$project_root"/EIDAS-IdP-1.0/target/IdP.war "$CATALINA_HOME/webapps"

export EIDAS_KEYSTORE='keystore/eidasKeystore.jks'
export EIDAS_HOST='http://127.0.0.1:8080'
export IDP_URL='http://127.0.0.1:8080'
export IDP_SSO_URL='https://127.0.0.1:8080'

# ---------------------------
# Start Tomcat
# ---------------------------

catalina run
