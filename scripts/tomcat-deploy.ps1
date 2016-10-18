if (!$env:CATALINA_HOME) {
  Write-Error "Please provide a value for the CATALINA_HOME environment variable."
  exit 1
}

$scriptPath = $MyInvocation.MyCommand.Definition
$parentPath = Split-Path -parent $scriptPath
$project_root = Split-Path -parent $parentPath

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
$env:EIDAS_KEYSTORE="$project_root/EIDAS-Node/target/EidasNode/WEB-INF/eidasKeystore.jks"
$env:EIDAS_HOST="http://127.0.0.1:8080"
$env:IDP_URL="http://127.0.0.1:8080"
$env:IDP_SSO_URL="https://127.0.0.1:8080"
$env:EIDAS_CONFIG_REPOSITORY="$project_root/EIDAS-Config/"

# Start Tomcat
Invoke-Expression "$env:CATALINA_HOME\bin\startup.bat"
