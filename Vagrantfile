# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.box = "liatrio/centos7chefclient"

  config.berkshelf.enabled = true
  config.vm.provision "chef_solo" do |chef|
    chef.add_recipe "jenkins-liatrio::default"
    chef.add_recipe "jenkins-liatrio::install_plugins"
    chef.add_recipe "jenkins-liatrio::create_creds"
    chef.add_recipe "minitest-handler"
    chef.json = {
      "jenkins" => {
        "master" => {
          "host" => "localhost",
#          "port" => 8083,
          #"repostiroy" => "http://pkg.jenkins-ci.org/redhat",
          "version" => "1.651-1.1"
        }
      },
#      "jenkins_liatrio" => {
#        "install_plugins" => {
#          "enablearchiva" => true,
#          "maven_mirror" => "http://localhost:8081/repository/internal",
#          "enablesonar" => true,
#          "sonarurl" => "http://localhost:9000",
#          "sonarjdbcurl" => "tcp://localhost:9092/sonar",
#          "githuburl" => "https://github.com/drewliatro/spring-petclinic/",
#          "giturl" => "https://github.com/drew-liatrio/spring-petclinic.git",
#          "hygieiaurl" => "http://192.168.100.10:8080/api/"
#        }
      }
  end

  config.vm.network :private_network, ip: "192.168.100.10"
  config.vm.network "forwarded_port", guest: 80, host: 18080

  config.vm.provider :virtualbox do |v|
    v.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
  end

  config.vm.provision "shell", inline: "firewall-cmd --permanent --add-port=8080/tcp && firewall-cmd --reload"

end
