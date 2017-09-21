import com.relus.AemCommands

def call(String name = "" ) {
    
    
    def server = Artifactory.server 'artifactory-server'
    rtMaven = Artifactory.newMavenBuild()
    rtMaven.tool = 'M3' // Tool name from Jenkins configuration
    rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
    rtMaven.resolver releaseRepo: 'libs-releases', snapshotRepo: 'libs-snapshot', server: server

    // now build, based on the configuration provided
    stage ("build") {
      //rtMaven.run pom: 'pom.xml', goals: 'clean test'
      AemCommands.build rtMaven
    }
}