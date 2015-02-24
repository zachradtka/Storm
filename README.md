# Storm
Examples using Apache Storm

## Installing Storm Server

Instructions modified from original source: http://www.michael-noll.com/tutorials/running-multi-node-storm-cluster/

### Create A Strorm User Account
    $ sudo groupadd storm
    $ sudo mkdir /var/lib/storm
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

Install supervisor

    $ sudo yum install supervisor
    $ sudo chkconfig supervisord on
    # Recommended: secure supervisord configuration file (may contain user credentials)
    $ sudo chmod 600 /etc/supervisord.conf


Next, we need to create a directory to log output from Storm daemons captured by supervisord. We will create directories under /var/log for this purpose. It is important to note that these logs only contain information from the Strom daemons, the actual logs for Storm are located under $STORM_HOME/logs/.

    $ sudo mkdir -p /var/log/storm
    $ sudo chown -R storm:storm /var/log/storm


## Configure supervisord for Storm

Add the following lines to /etc/supervisord.conf

    [program:storm-nimbus]
    command=/usr/local/apache-storm/default/bin/storm nimbus
    user=storm
    autostart=true
    autorestart=true
    startsecs=10
    startretries=999
    log_stdout=true
    log_stderr=true
    logfile=/var/log/storm/nimbus.out
    logfile_maxbytes=20MB
    logfile_backups=10

    [program:storm-supervisor]
    command=/usr/local/apache-storm/default/bin/storm supervisor
    user=storm
    autostart=true
    autorestart=true
    startsecs=10
    startretries=999
    log_stdout=true
    log_stderr=true
    logfile=/var/log/storm/supervisor.out
    logfile_maxbytes=20MB
    logfile_backups=10

    [program:storm-ui]
    command=/usr/local/apache-storm/default/bin/storm ui
    user=storm
    autostart=true
    autorestart=true
    startsecs=10
    startretries=999
    log_stdout=true
    log_stderr=true
    logfile=/var/log/storm/ui.out
    logfile_maxbytes=20MB
    logfile_backups=10

### Starting Supervisord

Before starting anythin, ensure that Storm is not currently running. If it is it must be killed before supervsord can be started.

To start supervisor use the following command
    
    sudo service supervisord start

To check the status of processes running under supervisord the supervisorctl command can be used.

    $ sudo supervisorctl status
    storm-nimbus   RUNNING    pid 16864, uptime 0:00:42
    storm-supervisor RUNNING    pid 16863, uptime 0:00:42
    storm-ui       RUNNING    pid 16862, uptime 0:00:42



