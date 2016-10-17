if (!$env:CATALINA_HOME) {
  Write-Error "Please provide a value for the CATALINA_HOME environment variable."
  exit 1
}

$scriptPath = $MyInvocation.MyCommand.Definition
$parentPath = Split-Path -parent $scriptPath
$project_root = Split-Path -parent $parentPath

# 2.2.1. Configuring Tomcat 7/Tomcat 8
# 1. Create a folder named endorsed in $TOMCAT_HOME.
# 2. Create a folder named shared in $TOMCAT_HOME.
if(!(Test-Path -Path endorsed )){
    New-Item -ItemType directory -Path $env:CATALINA_HOME/endorsed
}
if(!(Test-Path -Path shared/lib )){
    New-Item -ItemType directory -Path $env:CATALINA_HOME/shared/lib
}


# 3. Edit the file $TOMCAT_HOME\conf\catalina.properties
# and change the property shared.loader so that it reads:
# shared.loader=${catalina.home}\shared\lib\*.jar.
# Note that you need a minimum of Powershell 3.0 to make this work
# shared.loader=${catalina.home}/shared/lib/*.jar.

(gc $env:CATALINA_HOME/conf/catalina.properties | %{ $_ -replace "shared.loader=.*",'shared.loader=${catalina.home}/shared/lib/*.jar' }) | sc $env:CATALINA_HOME/conf/catalina.properties


# Extract from the binary zip file (under AdditionalFiles\endorsed)
# the following libs to $TOMCAT_HOME\shared\lib:
# TODO: consider making these jar files properly declared dependencies in pom.xml (if they're published to maven central)

Copy-Item $project_root/AdditionalFiles/endorsed/*.jar "$env:CATALINA_HOME/shared/lib"


# ---------------------------
# Rebuild Everything
# ---------------------------
mvn clean install -file "$project_root/EIDAS-Parent" -P embedded -P coreDependencies -D maven.test.skip=true

#Stop Tomcat
Invoke-Expression "$env:CATALINA_HOME\bin\shutdown.bat"

#Clean out tomcat deployment directory
Remove-Item $env:CATALINA_HOME\webapps\*.war
Remove-Item $env:CATALINA_HOME\webapps\SP -Recurse
Remove-Item $env:CATALINA_HOME\webapps\EidasNode -Recurse
Remove-Item $env:CATALINA_HOME\webapps\IdP -Recurse


# ---------------------------
# Deploy the Service Provider
# ---------------------------

# Deploy the SP
Copy-Item "$project_root/EIDAS-SP/target/SP.war" "$env:CATALINA_HOME\webapps"

# ---------------------------
# Deploy the Connector Node
# ---------------------------

$env:EIDAS_CONFIG_REPOSITORY="$project_root/EIDAS-Config/"

# Deploy the Node
Copy-Item "$project_root/EIDAS-Node/target/EidasNode.war" "$env:CATALINA_HOME\webapps"

# ---------------------------
# Deploy the IdP
# ---------------------------

# Deploy the IdP
Copy-Item "$project_root/EIDAS-IdP-1.0/target/IdP.war" "$env:CATALINA_HOME\webapps"

# ---------------------------
# Start Tomcat
# ---------------------------

# Temporarily setting env variable for local tomcat running in windows
$env:EIDAS_KEYSTORE="keystore/eidasKeystore.jks"
$env:EIDAS_HOST="http://127.0.0.1:8080"
$env:IDP_URL="http://127.0.0.1:8080"
$env:IDP_SSO_URL="https://127.0.0.1:8080"

# Start Tomcat
Invoke-Expression "$env:CATALINA_HOME\bin\startup.bat"
