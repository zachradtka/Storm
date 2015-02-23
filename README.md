# Storm
Examples using Apache Storm

## Installing Storm Server

Instructions modified from original source: http://www.michael-noll.com/tutorials/running-multi-node-storm-cluster/

### Create A Strorm User Account
    $ sudo groupadd storm
    $ sudo useradd -g storm -d /var/lib/storm -s /bin/bash storm -c "Storm service account"
    $ sudo chown -R storm:storm /var/lib/storm
    $ sudo chage -I -1 -E -1 -m -1 -M -1 -W -1 -E -1 storm
    
If not already done, 

    $ sudo chown -R storm:storm apache-storm-0.9.3
    

Create a local directory in which the Nimbus and Supervisor daemons can store (small) amounts of state such as jar files and configurations.

    $ sudo mkdir -p /opt/storm
    $ sudo chown -R storm:storm /opt/storm


storm.yaml

    storm.zookeeper.servers: 
        - "localhost"

    nimbus.host: "localhost"

    storm.local.dir: "/opt/storm"


