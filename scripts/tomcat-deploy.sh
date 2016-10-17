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

# Deploy the Node
cp "$project_root"/EIDAS-Node/target/EidasNode.war "$CATALINA_HOME/webapps"

# ---------------------------
# Deploy the IdP
# ---------------------------

# Deploy the IdP
cp "$project_root"/EIDAS-IdP-1.0/target/IdP.war "$CATALINA_HOME/webapps"

export EIDAS_CONFIG_REPOSITORY="$project_root"/EIDAS-Config/
export EIDAS_KEYSTORE='keystore/eidasKeystore.jks'
export EIDAS_HOST='http://127.0.0.1:8080'
export IDP_URL='http://127.0.0.1:8080'
export IDP_SSO_URL='https://127.0.0.1:8080'

# ---------------------------
# Start Tomcat
# ---------------------------

catalina run
