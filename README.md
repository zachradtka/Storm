# Storm

This repository is dedicated to Apache Storm projects and code samples.

## About Storm

To learn more about Storm, it is probably best to read the [short tutorial](https://storm.apache.org/documentation/Tutorial.html) on Storm's official web page. This is a nice guide and has some really good links. It is also not to long and easy to understand.

## Running The Examples

Before doing anything it is important to know that Storm can run topologies in local mode or distributed mode. Running a topology in local mode means that your code is running on a simulated a Storm cluster, while running in distributed mode means your code is running on a live Storm cluster.

### Local Mode

TODO: Add some notes about running things in local mode

### Distributed Mode

To run topologies in distributed mode, you need a Storm cluster, or just a VM. The following sections will explain how to install Storm on a Linux VM.

#### Installing Storm Server

These instructions were complied from reading a lot of documentation and web searching. The most important resource though was a blogpost from [Michael Noll's](http://www.michael-noll.com/tutorials/running-multi-node-storm-cluster/).

##### Download and Unpack Storm

First things first, download a release from the [Storm download page](https://storm.apache.org/downloads.html).

After downloading, unpack and create some symlinks to make things a bit easier. I like to unpack new things in ```/usr/local```, but you can place Storm wherever you like. ```<path_to_storm>``` and ```<version>``` are just the path where Storm was downloaded to and the version of Storm downloaded, respectively.

```
$ sudo mkdir /usr/local/apache-storm
$ sudo tar xzvf /<path_to_storm>/apache-storm-<version>.tar.gz -C /usr/local/apache-storm
$ sudo ln -s /usr/local/apache-storm/storm-<version> /usr/local/apache-storm/latest
$ sudo ln -s /usr/local/apache-storm/latest /usr/local/apache-storm/default

```

##### Create A Strom User Account

Now that Storm is nice and cozy in its new home, creating a Storm user will make running Storm easier. To create a storm user, use the following commands.

```
$ sudo groupadd storm
$ sudo mkdir /var/lib/storm
$ sudo useradd -g storm -d /var/lib/storm -s /bin/bash storm -c "Storm service account"
$ sudo chown -R storm:storm /var/lib/storm
$ sudo chage -I -1 -E -1 -m -1 -M -1 -W -1 -E -1 storm
```

Once the Storm user is created, make Storm the owner of it's files. Once again ```<version>``` is the version of storm that was downloaded in the previous step.

```
$ sudo chown -R storm:storm /usr/local/apache/apache-storm-<version>
``` 

##### Making The Binaries Accessible

One of the things I like to do when installing new software is add some environment variables to make accessing and using the software easier. The best way I have found to do this is to create a file in ```/etc/profile.d``` and export environment variables. You can do this in your personal ```~/.bashrc```, but putting things in ```/etc/profile.d``` makes them global, and I think is a bit cleaner for a test system.

So, create a file called *storm.sh* in ```/etc/profile.d```

```
$ sudo vi /etc/profile.d/storm.sh
```

And add the following

```
export STORM_HOME=/usr/local/apache-storm/default
export STORM_BIN=${STORM_HOME}/bin
export PATH=${PATH}:${STORM_BIN}
```

Once the copying and pasting is done, save and close the file. Now, Storm will be available in every future login. To make the changes available immediately, source the file you just created.

```
$ source /etc/profile.d/storm.sh
```

##### Storm Storage Directory

Storm, like most programs will need a directory to store small amounts of data. When Storm is running it uses a storage directory to store jar files and configurations. I created this directory under ```/opt``` and gave the Storm user ownership of this directory.

```
$ sudo mkdir -p /opt/storm
$ sudo chown -R storm:storm /opt/storm
```

##### Edit the Storm Config

Storm's configuration data is stored in ```$STORM_HOME/conf/storm.yaml```. For a single node installation, the following information needs to be added to ```storm.yaml```.

```
storm.zookeeper.servers: 
    - "localhost"

nimbus.host: "localhost"

storm.local.dir: "/opt/storm"
```

##### Starting and Stopping Storm

The installation should be complete and before going any further, now is a great time to boot up Storm and make sure everything is working fine. To start Storm, three processes need to be started, *Nimbus*, *Supervisor*, and *UI*. Use the following command to start Storm.

```
$ sudo -u storm -i storm nimbus &
$ sudo -u storm -i storm supervisor &
$ sudo -u storm -i storm ui &

```

The previous command will have some output, but that output should just be the commands and classpath to run Storm. After a minute or two the UI should be up and running you can view it by going to [http://localhost:8080/index.html](http://localhost:8080/index.html) in your browser.

##### Running Storm Under Supervision

Running Storm from the command line is a great, but Storm is a fail-fast system, which means that if anything bad is going to happen, the entire process will fail. Data will get replayed, so nothing will be lost, but it is tedious to manually restart the process.

To solve this problem, we can run Storm under supervision. All this means, is that a processes is constantly checking on Storm and restarting it if necessary. The software I used to perform supervision is supervisor.

To install supervisor

```
$ sudo yum install supervisor
$ sudo chkconfig supervisord on
# Recommended: secure supervisord configuration file (may contain user credentials)
$ sudo chmod 600 /etc/supervisord.conf
```

Next, a logging directory is needed by supervisord to capture output from Storm daemons. I put these directories under ```/var/log```, but you can put them wherever you would like. 

**Important:** These logs are supervisord's logs, not Storms logs. The logs for Storm can be found under ```$STORM_HOME/logs```. 


    $ sudo mkdir -p /var/log/storm
    $ sudo chown -R storm:storm /var/log/storm


##### Configure supervisord for Storm

To configure supervisord to watch Storm, add the following lines to ```/etc/supervisord.conf```

```
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
```

##### Starting Supervisord

Before starting anything, ensure that Storm is not currently running. If it is it must be killed before supervsord can be started.

To start supervisor use the following command

```
$ sudo service supervisord start
```

To check the status of processes running under supervisord the supervisorctl command can be used.

```
$ sudo supervisorctl status
storm-nimbus   RUNNING    pid 16864, uptime 0:00:42
storm-supervisor RUNNING    pid 16863, uptime 0:00:42
storm-ui       RUNNING    pid 16862, uptime 0:00:42
```

## The Examples

The following is a listing of examples in this repository.

### Basic Append Topology

This example uses a single bolt to append a string onto incoming tuples. For example if the value coming in is "tacos" and the value being appended is " is awesome", the output of the bolt will be "tacos is awesome".

This topology can be run locally or remotely and takes the following command line arguments

```
OPTIONS
    -n=NAME
        set the topology name to NAME
    -r
        execute on a remote Storm cluster
    -v=VALUE
        append VALUE to the end of words

```

#### Examples

Run in local mode (LocalCluster), with topology name "appendTopology"

```
$ storm jar storm-examples-jar-with-dependencies.jar storm.BasicAppendTopology
```

Runs in local mode (LocalCluster), with topology append value "?"
```
$ storm jar storm-examples-jar-with-dependencies.jar storm.BasicAppendTopology -v=?
```
Runs in remote/cluster mode, with topology name "production-topology"

```
$ storm jar storm-examples-jar-with-dependencies.jar storm.BasicAppendTopology -v=? -n=production-topology -r
```

### WordCount

