import groovy.json.JsonSlurper
import java.io.FileReader;
import java.nio.file.*
import groovy.json.*
import groovy.runtime.*;

DEV_BOX = true // DEV_BOX disables all generated jobs by default
GIT_API = "https://api.github.com/repos/"
GIT_URL = "https://github.com/"
GIT_AUTH_TOKEN = "empty"
USE_FOLDERS = true //Assumes you're using the Folders plugin

String fileName = "buildDeployPipelines.json"
def file = readFileFromWorkspace(fileName)
def inputJson = new JsonSlurper().parseText(file)

def components =  inputJson.components
for( component in components ) {
  if (USE_FOLDERS)
  {
    USE_FOLDERS = createFolders(component.scmProject, component.productName)
    out.println("Using folders = " + USE_FOLDERS)
    //should be true if the plugin exists
  }

  def deploymentEnvironments = component.deploymentEnvironments
  for(env in deploymentEnvironments) {
    createDeployJob(component.productName, component.scmProject, env)
  }
  createBuildJob( component )
}

def createFolders(project, product)
{
  def productPath = project + "/" + product
  try{
    def createProjectFolder = folder(project)
    def createProductFolder = folder(productPath)
    def createProductBuildsFolder = folder(productPath + "/builds")
    def createProductDeploymentsFolder = folder(productPath + "/deployments")
  }
  catch (Exception exception){
    return false
  }
  return true
}

def createDeployJob(productName, projectName, environment) {
  def deployJobName = createDeployJobName(projectName, productName, environment)
  def jobLocation = ""

  if (USE_FOLDERS)
  {
    jobLocation = projectName + "/"+ productName + "/deployments/"
  }

  job(jobLocation + deployJobName) {
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

  if (auth.size() > 10) //Just looking for something that looks real
  {
    out.println("The git auth token was provided.  Using it...")
    try
    {
      return json.parse(branchApi.toURL().newReader(requestProperties: ["Authorization": "token ${auth}".toString(), "Accept": "application/json"]))
    }
    catch (Exception ex)
    {
      out.println(ex)
      return null //API request failed
    }
  }
  else
  {
    try
    {
      return json.parse(branchApi.toURL().newReader())
    }
    catch (Exception ex)
    {
      out.println(ex)
      out.println("Auth likely failed - Provide an api key if repository is private.")
      return null //API request failed
    }
  }
}

def createBuildJob(component) {
  String productPath = component.scmProject + "/" + component.productName
  String branchApi =  GIT_API + productPath + "/branches"
  String repoUrl = GIT_URL + productPath
  def ciEnvironments = component.ciEnvironments
  def downStreamJobs = []

  for(env in ciEnvironments)
    downStreamJobs.add( createDeployJobName(component.scmProject, component.productName, env) )

  def branches = getBranches(branchApi)
  if (branches)
  {
    branches.each {
        def branchName = it.name
        def jobName = "${component.scmProject}-${component.productName}-${branchName}-build".replaceAll('/','-').toLowerCase()
        def jobLocation = ""
        if (USE_FOLDERS)
        {
          jobLocation = productPath + "/builds/"
        }
        out.println("Creating or updating job " + jobLocation )
        mavenJob(jobLocation + jobName) {
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
}
