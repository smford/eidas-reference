#!/usr/bin/env bash

mvn --file EIDAS-Parent clean install -P embedded -P coreDependencies -Dmaven.test.skip=true
