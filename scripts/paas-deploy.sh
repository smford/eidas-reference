#!/usr/bin/env bash

scripts_dir=$(dirname "$0")
project_root="$scripts_dir"/..

mvn clean package --file "$project_root/EIDAS-Parent" -P embedded -P coreDependencies -Dmaven.test.skip=true

cf push stub-sp-reference           -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-SP/target/SP.war"
cf push uk-connector-node-reference -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-Node/target/EidasNode.war"
cf push es-proxy-node-reference     -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-Node/target/EidasNode.war"
cf push stub-idp-reference          -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-IdP-1.0/target/IdP.war"

