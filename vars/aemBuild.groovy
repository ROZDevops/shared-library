import com.relus.AemCommands

def call(String name = "" ) {
    
    def helper = AemCommands.getInstance(this);

    stage ("build") {
      helper.build()
    }
}