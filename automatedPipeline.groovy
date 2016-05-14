import groovy.json.JsonSlurper
import java.io.FileReader;
import java.nio.file.*
import groovy.json.*

DEV_BOX = "true"
GIT_API = "https://api.github.com/repos/"
GIT_URL= "https://github.com/"
String fileName = "buildDeployPipelines.json"
def file = readFileFromWorkspace(fileName)
def inputJson = new JsonSlurper().parseText(file)

def components =  inputJson.components
for( component in components ) {
  def deploymentEnvironments = component.deploymentEnvironments
  for(env in deploymentEnvironments) {
    createDeployJob( component.productName, component.scmProject , env)
  }
  createBuildJob( component )
}

def createDeployJob(productName, projectName, environment) {
  def deployJobName = createDeployJobName(projectName, productName, environment)
  job(deployJobName) {
    if(DEV_BOX)
    {
      disabled()
    }
    description("<h3>This job was created by automation.  Manual edits to this job are discouraged.</h3> ")
    steps {
      label('master')
    }
  }
  return deployJobName
}

def createDeployJobName(projectName, productName , environment) {
  return (projectName + "-" + productName +"-deploy-" + environment).toLowerCase()
}

def getBranches(branchApi) {
  def auth = "ca5150b27ad3138cf12c6a5e9c24dd5b88b926b5"
  def json = new JsonSlurper()
  return json.parse(branchApi.toURL().newReader(requestProperties: ["Authorization": "token ${auth}".toString(), "Accept": "application/json"]))
}

def createBuildJob(component) {
  String branchApi =  GIT_API + component.scmProject + "/" + component.productName + "/branches"
  String repoUrl = GIT_URL + component.scmProject + "/" + component.productName
  def ciEnvironments = component.ciEnvironments
  def downStreamJobs = []
  for(env in ciEnvironments)
    downStreamJobs.add( createDeployJobName(component.scmProject, component.productName, env) )

  def branches = getBranches(branchApi)
  branches.each {
        def branchName = it.name
        def jobName = "${component.scmProject}-${component.productName}-${branchName}-build".replaceAll('/','-').toLowerCase()
        mavenJob(jobName) {
            if(DEV_BOX)
            {
              disabled()
            }
            description("<h3>This job was created with automation.  Manual edits to this job are discouraged.</h3> ")
            scm {
                git(repoUrl, branchName)
            }
            triggers {
              scm('H/2 * * * *')
            }
            mavenInstallation('maven 3')
            goals("clean deploy")
             postBuildSteps('SUCCESS') {

              if(downStreamJobs && (branchName == "master")) {
                downstreamParameterized {
                  trigger(downStreamJobs.join(", ")) {
                  }
                }
              }
            }
        }
      return jobName
    }
}
