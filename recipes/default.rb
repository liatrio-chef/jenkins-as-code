#
# Cookbook Name:: jenkins-as-code
# Recipe:: default
#
# All rights reserved - Do Not Redistribute
#

include_recipe 'jenkins-liatrio::default'
include_recipe 'jenkins-liatrio::install_plugins'
include_recipe 'jenkins-liatrio::create_creds'
include_recipe 'jenkins-liatrio::job_seed_automation'
