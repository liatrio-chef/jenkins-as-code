require 'minitest/spec'

def service_is_listening( port, service )
  assert system "sudo netstat -lp --numeric-ports | grep \":#{port}.*LISTEN.*#{service}\""
end

def web_check_match( url, check )
  assert system "wget -q -O - #{url} | grep '#{check}'"
end

describe_recipe 'apache2-liatrio::default' do

  it "listens for http on tcp port 80" do
    service_is_listening("80", "httpd")
  end

  #it 'must match the web check Apache' do
  #  web_check_match("http://127.0.0.1/", "Apache")
  #end

end
