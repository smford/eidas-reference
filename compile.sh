#!/usr/bin/env bash
set -e
mvn --file EIDAS-Parent                          clean install -Dmaven.test.skip=true
mvn --file EIDAS-Light-Commons                   clean install -Dmaven.test.skip=true
mvn --file EIDAS-Commons                         clean install -P embedded -Dmaven.test.skip=true
mvn --file EIDAS-SpecificCommunicationDefinition clean install -Dmaven.test.skip=true
mvn --file EIDAS-Encryption                      clean install -Dmaven.test.skip=true
mvn --file EIDAS-ConfigModule                    clean install -Dmaven.test.skip=true
mvn --file EIDAS-SAMLEngine                      clean install -Dmaven.test.skip=true
mvn --file EIDAS-Updater                         clean install -Dmaven.test.skip=true
mvn --file EIDAS-Specific                        clean install -P embedded -Dmaven.test.skip=true
mvn --file EIDAS-Node                            clean package -P embedded -Dmaven.test.skip=true
mvn --file EIDAS-SP                              clean package -P embedded -P coreDependencies -Dmaven.test.skip=true

