package com.relus

class AemCommands implements Serializable{

  def script
  def artifactory
  def server
  def rtMaven

  private static AemCommands instance
  
  static AemCommands getInstance(args) { 
    if (!instance) {
      args.echo "    -> new instance"
      instance = new AemCommands(args)
    }else{
      args.echo "    -> old instance"
    }
    return instance
  }

  private AemCommands(script) {
    this.script = script
    artifactory = script.Artifactory
    server = artifactory.server 'artifactory-server'
    rtMaven = artifactory.newMavenBuild()
    rtMaven.tool = mvn_id // Tool name from Jenkins configuration
    rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
    rtMaven.resolver releaseRepo: 'libs-releases', snapshotRepo: 'libs-snapshot', server: server
  }

  def build() {
    rtMaven.run pom: 'pom.xml', goals: 'clean test'
  }

  def flushCache(aemDispatcherUser, aemDispatcherPassword, dispatcherNode) {
      sh "curl -k -u \\\"\\$aemDispatcherUser\\\":\\$aemDispatcherPassword -X POST -H \\\"CQ-Action: deactivate\\\" -H \\\"CQ-Handle: /\\\" -H \\\"Content-Length: 0\\\" http://$dispatcherNode/dispatcher/invalidate.cache"
  }

  def flushServerList(aemDispatcherUser, aemDispatcherPassword, serverList) {
      serverList.each {
          flushCache(aemUser, aemPassword, it)
      }
  }

  def deploy(AEM_USER, AEM_PASSWORD, modules, String params= "-P presentationTooling -D presentation.mode=normal -P autoInstallPackage", autoInstallContent ){
    def pom = script.readMavenPom file: 'pom.xml'
    for (art_id in modules){
      /*rtMaven.run pom: 'pom.xml', goals: */
      script.echo "com.day.jcr.vault:content-package-maven-plugin:install \
        ${params} ${autoInstallContent?'-P autoInstallContent':''} \
        -Daem.host=author1-patch.aws.mbusa.com \
        -Daem.port=4502 \
        -Dvault.userId=\"${AEM_USER}\" \
        -Dvault.password=\"${AEM_PASSWORD}\" \
        -Dvault.groupId=com.mb.oneweb \
        -Dvault.artifactId=${art_id} \
        -Dvault.version=${pom.version} \
        -Dvault.failOnError=true \
        -Dvault.failOnMissingEmbed=true \
        -Dvault.verbose=true \
        -U".toString()
    }

    
    script.echo "autoInstallContent set to ${autoInstallContent?'SI':'NO'}"
    
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

  /*def deployPublish(aemUser, aemUser, hostProtocol, port, publishHost, publishProtocol, autoInstallContentPublish){
    
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
  }*/
}
