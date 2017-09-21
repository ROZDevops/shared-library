def call(String name = "" ) {
    
    
    def server = Artifactory.server 'artifactory-server'


    // now build, based on the configuration provided
    stage ("build") {
      echo "aaaa"
    }
}