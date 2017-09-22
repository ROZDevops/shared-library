import com.relus.AemCommands

def call(String credentialId = "") {
    
  def helper = AemCommands.getInstance(this);

  stage ("Deploy") {
  	withCredentials([
      [$class: 'UsernamePasswordMultiBinding', credentialsId: credentialId, usernameVariable: 'AEM_USER', passwordVariable: 'AEM_PASSWORD'],
		]) {
    	helper.deploy(AEM_USER, AEM_PASSWORD, ["mb-nafta.ui.content", "mb-nafta.ui.apps", "mb-nafta.vehicles-data.apps", "mb-nafta.vehicles-data.content"])
    }
  }
}