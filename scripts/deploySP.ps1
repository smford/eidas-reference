# deploy app to clound foundry

#find 
$scriptPath = $MyInvocation.MyCommand.Definition
$parentPath = Split-Path -parent $scriptPath
$project_root = Split-Path -parent $parentPath


#create war file with maven build
mvn clean package -file "$project_root/EIDAS-Parent" -P cloudF -P embedded -P coreDependencies


cf push niyi-stub-sp -f $project_root/scripts/manifest.yml -p $project_root/EIDAS-SP/target/SP.war

cf push niyi-stub-node -f $project_root/scripts/manifest.yml -p $project_root/EIDAS-Node/target/EidasNode.war

cf push niyi-stub-idp -f $project_root/scripts/manifest.yml -p $project_root/EIDAS-IdP-1.0/target/IdP.war