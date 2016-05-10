import groovy.json.JsonSlurper
import java.io.FileReader;
import java.nio.file.*
import groovy.json.*


GIT_URL = "https://github.com/"
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
  createUCDComponent(component.productName)
}

def createDeployJob(productName, projectName, environment) {
  def deployJobName = createDeployJobName(projectName, productName, environment)
  job(deployJobName) {
    description("<h2>This job was created with automation.  Manual edits to this job are discouraged.</h2> ")

    steps {
      label('master')
    }
  }
  return deployJobName
}
def createDeployJobName(projectName, productName , environment) {
  return projectName + "-" + productName +"-deploy-" + environment
}

def createBuildJob(component) {
  String branch = component.branch? component.branch : 'master'
  String buildSuffix = branch == 'master'? "-build" : "-" + branch.replaceAll('/','-') + "-build"
  String buildJobName = component.scmProject + "-" + component.productName + buildSuffix
  String gitUrl =  GIT_URL + component.scmProject + "/" + component.productName + ".git"
  def ciEnvironments = component.ciEnvironments
  def downStreamJobs = []
  for(env in ciEnvironments)
    downStreamJobs.add( createDeployJobName(component.scmProject, component.productName, env) )

  mavenJob(buildJobName) {
    description("<h2>This job was created with automation.  Manual edits to this job are discouraged.</h2> ")
    label('master')
    scm {
      git(gitUrl, branch)
    }
    triggers {
      scm('H/2 * * * *')
    }
    mavenInstallation('maven 3')
    goals("clean deploy")
     postBuildSteps('SUCCESS') {
      environmentVariables {
        propertiesFile('propsfile')
      }
      if(downStreamJobs) {
        downstreamParameterized {
          trigger(downStreamJobs.join(", ")) {
          }
        }
      }
    }
  }
  return buildJobName
}
