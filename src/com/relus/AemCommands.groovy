package com.relus

def flushCache(aemDispatcherUser, aemDispatcherPassword, dispatcherNode) {
    sh "curl -k -u \\\"\\$aemDispatcherUser\\\":\\$aemDispatcherPassword -X POST -H \\\"CQ-Action: deactivate\\\" -H \\\"CQ-Handle: /\\\" -H \\\"Content-Length: 0\\\" http://$dispatcherNode/dispatcher/invalidate.cache"
}

def flushServerList(aemDispatcherUser, aemDispatcherPassword, serverList) {
    serverList.each {
        flushCache(aemUser, aemPassword, it)
    }
}

def build(rtMaven){
  rtMaven.run pom: 'pom.xml', goals: 'clean test'
}


def deploy(rtMaven, modules){
  for (art_id in ["mb-nafta.ui.content", "mb-nafta.ui.apps", "mb-nafta.vehicles-data.apps", "mb-nafta.vehicles-data.content"]){
    rtMaven.run pom: 'pom.xml', goals: "com.day.jcr.vault:content-package-maven-plugin:install \
      -P presentationTooling -D presentation.mode=normal -P autoInstallPackage -P autoInstallContent \
      -Daem.host=author1-patch.aws.mbusa.com \
      -Daem.port=4502 \
      -Dvault.userId=\"${AEM_USER}\" \
      -Dvault.password=\"${AEM_PASSWORD}\" \
      -Dvault.groupId=com.mb.oneweb \
      -Dvault.artifactId=${art_id} \
      -Dvault.version=2.1.0 \
      -Dvault.failOnError=true \
      -Dvault.failOnMissingEmbed=true \
      -Dvault.verbose=true \
      -U".toString()
  }
}

def deployContent(aemUser, aemPassword, host, port, autoInstallContent, hostProtocol){
  
  //autoInstallContent and hostProtocol are true or false, we create the string chain depending on those values
  
    withMaven(maven: 'apache-maven-3.3.9', globalMavenSettingsConfig: 'a4511c59-8ad2-4dde-ad4b-29a7f99a4c55'){
      
    //DEV
    if ( autoInstallContent ){
      if ( hostProtocol ) {
        sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackage -P autoInstallContent -Daem.host.protocol=http -Daem.host=${host} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
        return
      }
      sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackage -P autoInstallContent -Daem.host=${host} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
      return
      //QA to PROD
    } else if ( hostProtocol ){ //QA-GH to prod
    
      sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackage -Daem.host.protocol=${hostProtocol} -Daem.host=${host} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
      return
    } else { //QA only
    
    
      sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackage -Daem.host=${host} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
    }
 }
}

def deployPublish(aemUser, aemUser, hostProtocol, port, publishHost, publishProtocol, autoInstallContentPublish){
  
    withMaven(maven: 'apache-maven-3.3.9', globalMavenSettingsConfig: 'a4511c59-8ad2-4dde-ad4b-29a7f99a4c55'){
      
    if ( autoInstallContentPublish ) { //DEV
      sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackagePublish -P autoInstallContentPublish -Daem.publish.host=${publishHost} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
      return
      if ( hostProtocol ){ //DEV-GH
        sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackagePublish -P autoInstallContentPublish -Daem.host.protocol=http -Daem.publish.host.protocol=${publishProtocol} -Daem.publish.host=${publishHost} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
        
        return
      }
    } else if ( hostProtocol ){ //stage & prod
      sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackagePublish -Daem.host.protocol=http -Daem.publish.host.protocol=${publishProtocol} -Daem.publish.host=${publishHost} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
      return
    } else { // QA
      sh("mvn clean install -P presentationTooling -D presentation.mode=normal -P autoInstallPackagePublish -Daem.publish.host=${publishHost} -Daem.port=${port} -Dsling.user=\"\${aemUser}\" -Dsling.password=\"\${aemPassword}\"")
    }
  }
}


def deployPublishList(aemUser, aemUser, hostProtocol, port, publishHostList, publishProtocol, autoInstallContentPublish) {
  
  // This method receive the publishHostList with the list of publish host that is defined on jenkins-shared-libraries/vars/environments.groovy
  
    publishHostList.each {
      
        deployPublish(aemUser, aemUser, hostProtocol, port, it, publishProtocol, autoInstallContentPublish)
    }
}
