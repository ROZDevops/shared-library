import com.relus.AemCommands

def call(String stageName="build") {
    
    def helper = AemCommands.getInstance(this);

    stage (stageName) {
      helper.build()
    }
}
