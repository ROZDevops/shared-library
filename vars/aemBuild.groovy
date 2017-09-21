import com.relus.AemCommands

def call(String name = "" ) {
    
    def helper = AemCommands.getInstance(this);

    // now build, based on the configuration provided
    stage ("build") {
      helper.build()
    }
}