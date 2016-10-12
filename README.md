# eidas-reference [![Build Status](https://travis-ci.org/alphagov/eidas-reference.svg?branch=master)](https://travis-ci.org/alphagov/eidas-reference)

This repository contains a fork of the eIDAS reference implementation provided by CEF digital.
The original code can be obtained [here](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node).

*NOTE: It's highly recommended that you use the official reference implementation and not this fork.*

## Licence

This code maintains its original copyright (2016 European Commission) and is licenced under the
[European Union Public Licence](https://ec.europa.eu/cefdigital/wiki/download/attachments/30771884/eupl1.1.-licence-en_0.pdf).

## Getting Started

There are [thorough instructions for getting set up on a range of application servers](https://ec.europa.eu/cefdigital/wiki/download/attachments/30771884/eIDAS%20Node%20Installation%20Manual%20v1.1.pdf)
provided by CEF digital.

If you just want to get up and running quickly we recommend you use [tomcat](https://tomcat.apache.org/), for which we've provided some scripts to help get you started.

Regardless, you'll need to install [a version of the JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (1.7 or higher, we recommend 1.8)
with the [Unlimited Strength Jurisdiction Policy Extension](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) installed
(see [this blog post](http://suhothayan.blogspot.co.uk/2012/05/how-to-install-java-cryptography.html) for instructions).

### Running the tests

From the `EIDAS-Parent` folder run `mvn test`.

### Running the applications locally with Tomcat

*Note (windows): the scripts for windows use [Powershell](https://msdn.microsoft.com/en-us/powershell/mt173057.aspx).
If you haven't used Powershell before you'll need to [set your execution policy](https://technet.microsoft.com/en-us/library/hh849812.aspx)
to allow it to run unsigned scripts (from powershell run `Set-ExecutionPolicy Unrestricted`).
You also need to set CATALINA_HOME in Windows System Environment Variables - this should be your Tomcat root directory. 
The advisable tomcat downoad is the zipped one and not tomcat windows service.*

```
# Linux or OS X
./scripts/tomcat-deploy.sh

# Windows
.\scripts\tomcat-deploy.ps1
```

#### Where to find the applications

By default tomcat will run on port 8080, so if all has gone well the applications will be reachable at:

Stub Service Provider - [http://localhost:8080/SP](http://localhost:8080/SP)
eIDAS Node - [http://localhost:8080/EidasNode](http://localhost:8080/EidasNode)
Stub Identity Provider - [http://localhost:8080/IdP](http://localhost:8080/IdP)

### Editing the code

You can import the code into your IDE of choice by pointing it at the pom.xml in EIDAS-Parent.

