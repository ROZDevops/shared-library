import com.relus.AemCommands

def call(String name = "" ) {
    
    def helper = new AemCommands(this);

    // now build, based on the configuration provided
    stage ("build") {
      helper.build()
    }
}