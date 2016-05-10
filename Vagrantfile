# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.box = "liatrio/centos7chefclient"

  config.berkshelf.enabled = true
  config.vm.provision "chef_solo" do |chef|
    chef.add_recipe "jenkins-as-code::default"
    chef.add_recipe "jenkins-as-code::create_jobs"
    #chef.add_recipe "minitest-handler"
    chef.json = {
      "jenkins" => {
        "master" => {
          "host" => "localhost",
          "version" => "1.651-1.1"
        }
      },
      "jenkins_liatrio" => {
        "install_plugins" => {
           "plugins_list" => %w{git github job-dsl}
        }
      }
    }
  end

  config.vm.network :private_network, ip: "192.168.100.10"
  config.vm.network "forwarded_port", guest: 8080, host: 18080

  config.vm.provider :virtualbox do |v|
    v.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
  end

  config.vm.provision "shell", inline: "firewall-cmd --permanent --add-port=8080/tcp && firewall-cmd --reload"

end
