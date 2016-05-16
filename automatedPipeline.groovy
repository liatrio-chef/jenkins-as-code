import groovy.json.JsonSlurper
import java.io.FileReader;
import java.nio.file.*
import groovy.json.*
import groovy.runtime.*;

DEV_BOX = true
GIT_API = "https://api.github.com/repos/"
GIT_URL = "https://github.com/"
GIT_AUTH_TOKEN = null

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
  def auth = GIT_AUTH_TOKEN
  def json = new JsonSlurper()
  if (auth != null)
  {
    return json.parse(branchApi.toURL().newReader(requestProperties: ["Authorization": "token ${auth}".toString(), "Accept": "application/json"]))
  }
  else
  {
    return json.parse(branchApi.toURL().newReader())
  }
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

            if ( branchName == "master" )
              goals("clean deploy")
            else
              goals("clean install")

            postBuildSteps('SUCCESS') {

            if( downStreamJobs && branchName == "master" ) {
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
