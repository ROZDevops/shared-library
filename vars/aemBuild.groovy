import com.relus.AemCommands

def call(String stageName="build") {
    
    def helper = AemCommands.getInstance(this);

    stage (stageName) {
      echo libraryResource 'com/relus/defaults.json'
      helper.build()
    }
}
