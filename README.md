# eidas-reference

This repository contains a fork of the eIDAS reference implementation provided by CEF digital.
The original code can be obtained [here](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eIDAS-Node).

*NOTE: It's highly recommended that you use the official reference implementation and not this fork.*

## Licence

This code maintains its original copyright (2016 European Commission) and is licenced under the
[European Union Public Licence](https://ec.europa.eu/cefdigital/wiki/download/attachments/30771884/eupl1.1.-licence-en_0.pdf).

## Getting Started

There are thorough instructions for getting set up on a range of application servers [here](https://ec.europa.eu/cefdigital/wiki/download/attachments/30771884/eIDAS%20Node%20Installation%20Manual%20v1.1.pdf).
If you just want to get up and running quickly we recommend you use [tomcat](https://tomcat.apache.org/), for which we've provided some scripts to help get you started.

### Running the tests

From the `EIDAS-Parent` folder run `mvn test`.

### Running the applications locally

If you're using tomcat, simply run:

```
# If you're on *nix (e.g. OS X or Linux)
./scripts/tomcat-deploy.sh

# If you're on windows
# (you'll need to set your execution policy if you haven't used powershell before)
# (from powershell run `Set-ExecutionPolicy Unrestricted`)
.\scripts\tomcat-deploy.ps1
```

### Editing the code

You can import the code into your IDE of choice by pointing it at the pom.xml in EIDAS-Parent.

