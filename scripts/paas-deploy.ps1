# deploy app to clound foundry

#find 
$scriptsPath = $MyInvocation.MyCommand.Definition
$scripts_dir = Split-Path -parent $scriptsPath
$project_root = Split-Path -parent $scripts_dir


mvn clean package --file "$project_root/EIDAS-Parent" -P embedded -P coreDependencies -D maven.test.skip=true

cf push stub-sp-reference           -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-SP/target/SP.war"
cf push uk-connector-node-reference -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-Node/target/EidasNode.war"
cf push es-proxy-node-reference     -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-Node/target/EidasNode.war"
cf push stub-idp-reference          -f "$scripts_dir/manifest.yml" -p "$project_root/EIDAS-IdP-1.0/target/IdP.war"
