#!/usr/bin/env bash
set -e
cd EIDAS-Parent
mvn clean install
cd ..
cd EIDAS-Light-Commons
mvn clean install
cd ..
cd EIDAS-Commons
mvn clean install -P embedded
cd ..
cd EIDAS-SpecificCommunicationDefinition
mvn clean install
cd ..
cd EIDAS-Encryption
mvn clean install
cd ..
cd EIDAS-ConfigModule
mvn clean install
cd ..
cd EIDAS-SAMLEngine
mvn clean install
cd ..
cd EIDAS-Updater
mvn clean install
cd ..
cd EIDAS-Specific
mvn clean install -P embedded
cd ..
cd EIDAS-Node
mvn clean package -P embedded
cd ..
cd EIDAS-SP
mvn clean package -P embedded -P coreDependencies
cd ..
