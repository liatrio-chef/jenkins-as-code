#
# Cookbook Name:: jenkins-liatrio
# Attributes:: create_jobs
#
# Author: Drew Holt <drew@liatrio.com>
#

# Create AutomationManagementJob job
template '/var/lib/jenkins/jobs/AutomationManagementJob-config.xml' do
  source 'var/lib/jenkins/jobs/AutomationManagementJob-config.xml.erb'
  mode     '0644'
  owner node[:jenkins][:master][:user]
  group node[:jenkins][:master][:group]
  variables({
  })
end

jenkins_job 'AutomationManagementJob' do
  config '/var/lib/jenkins/jobs/AutomationManagementJob-config.xml'
end

