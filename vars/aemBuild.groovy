import com.relus.AemCommands

def call() {
    
    def helper = AemCommands.getInstance(this);

    stage ("build") {
      helper.build()
    }
}
