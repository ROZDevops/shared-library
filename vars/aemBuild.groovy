import com.relus.AemCommands

def call(String stageName="build") {
    
    def helper = AemCommands.getInstance(this);

    stage (stageName) {
      def request = libraryResource 'com/relus/defaults.json'
      echo request
      helper.build()
    }
}
