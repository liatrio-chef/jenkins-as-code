# -*- mode: ruby -*-
# vi: set ft=ruby :

# ensure berkshelf and hostupdate are installed
required_plugins = %w(vagrant-berkshelf vagrant-hostsupdater)
required_plugins.each do |plugin|
  exec "vagrant plugin install #{plugin};vagrant #{ARGV.join(' ')}" unless Vagrant.has_plugin? plugin || ARGV[0] == 'plugin'
end

Vagrant.configure(2) do |config|
  config.vm.box = 'bento/centos-7.2'

  config.berkshelf.enabled = true

  config.vm.provision 'chef_solo' do |chef|
    chef.add_recipe 'jenkins-as-code::default'
    chef.add_recipe 'jenkins-as-code::create_jobs'
    # chef.add_recipe "minitest-handler"
    chef.json = {
      'jenkins' => {
        'master' => {
          'host' => 'localhost',
          'version' => '1.651-1.1'
        }
      },
      'jenkins_liatrio' => {
        'install_plugins' => {
          'plugins_list' => %w(maven-plugin=2.7.1 cvs=2.11 ant=1.2 credentials=1.18 pam-auth=1.1 translation=1.10 junit=1.2-beta-4 matrix-auth=1.1 mailer=1.11 matrix-project=1.4.1 subversion=1.54 script-security=1.13 windows-slaves=1.0 ssh-slaves=1.9 antisamy-markup-formatter=1.1 ssh-credentials=1.10 external-monitor-job=1.4 javadoc=1.1 ldap=1.11 structs=1.2 workflow-step-api=1.15 workflow-scm-step=1.14.2 git-client=2.1.0 scm-api=1.3 git=3.0.1 github-api=1.69 plain-credentials=1.1 token-macro=1.5.1 github=1.23.1 job-dsl=1.53 envinject=1.93.1 run-condition=1.0 conditional-buildstep=1.3.1 parameterized-trigger=2.32 cloudbees-folder=5.13 jackson2-api=2.7.3 aws-java-sdk=1.11.37 copyartifact=1.38.1 s3=0.10.10)
        }
      }
    }
  end

  config.vm.network :private_network, ip: '192.168.100.10'
  config.vm.network 'forwarded_port', guest: 8080, host: 18_080

  config.vm.provider :virtualbox do |v|
    v.customize ['modifyvm', :id, '--natdnshostresolver1', 'on']
    v.customize ['modifyvm', :id, '--cableconnected1', 'on'] # fix for bento box issue https://github.com/chef/bento/issues/682
  end

  # config.vm.provision 'shell', inline: 'firewall-cmd --permanent --add-port=8080/tcp && firewall-cmd --reload'
end
