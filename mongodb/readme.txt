mongodb搭建高可用集群

目标：通过副本集和分片实现mongodb的高可用集群
    副本集     Replica Set 
        - 配置若干个节点对主节点数据进行复制。
        - 作用：当主节点宕机后，由仲裁节点选举将某个副本提升为Primary，确保了可用性。
    分片       Sharding    
        - 将数据分散到不同的节点存储
        - 将数据读写压力分摊到多个节点，解决了单机性能瓶颈问题
        - 数据完全分布式存储，可实现服务的水平扩展
        
    将 副本集 与 分片 两个功能结合起来，就可以实现集群的failover自动故障转移，同时也实现了数据的分布式存储。
    
-------------
数据存储（数据库）需要考虑的问题：
    主节点挂了能否自动切换连接？---是否存在单点问题？是否支持failover，自动故障切换？
    主节点的读写压力过大如何解决？---是否支持读写分离？
    从节点每个上面的数据都是对数据库全量拷贝，这种模式是否高效？ ---数据没有实现分布式存储！
    数据压力大到机器支撑不了的时候能否做到自动扩展？---单机存在性能瓶颈！
    
NoSQL的产生就是为了解决大数据量、高扩展性、高性能、灵活数据模型、高可用性。
但是光通过主从模式的架构远远达不到上面几点，由此MongoDB设计了副本集和分片的功能。


-------------
参考：http://www.ityouknow.com/mongodb/2017/08/05/mongodb-cluster-setup.html

Mongodb的四大组件：mongos、config server、shard、replica set。

mongos
    数据库集群请求的入口，所有的请求都通过mongos进行协调，不需要在应用程序添加一个路由选择器，mongos自己就是一个请求分发中心，
    它负责把对应的数据请求请求转发到对应的shard服务器上。
    在生产环境通常有多mongos作为请求的入口，防止其中一个挂掉所有的mongodb请求都没有办法操作。

config server
    顾名思义为配置服务器，存储所有数据库元信息（路由、分片）的配置。
    mongos本身没有物理存储分片服务器和数据路由信息，只是缓存在内存里，配置服务器则实际存储这些数据。
    mongos第一次启动或者关掉重启就会从 config server 加载配置信息，以后如果配置服务器信息变化会通知到所有的 mongos 更新自己的状态，
    这样 mongos 就能继续准确路由。在生产环境通常有多个 config server 配置服务器，因为它存储了分片路由的元数据，防止数据丢失！

shard，分片（sharding）
    是指将数据库拆分，将其分散在不同的机器上的过程。将数据分散到不同的机器上，不需要功能强大的服务器就可以存储更多的数据和处理更大的负载。
    基本思想就是将集合切成小块，这些块分散到若干片里，每个片只负责总数据的一部分，最后通过一个均衡器来对各个分片进行均衡（数据迁移）。

replica set
    中文翻译副本集，其实就是shard的备份，防止shard挂掉之后数据丢失。
    复制提供了数据的冗余备份，并在多个服务器上存储数据副本，提高了数据的可用性， 并可以保证数据的安全性。

仲裁者（Arbiter）
    是复制集中的一个MongoDB实例，它并不保存数据。仲裁节点使用最小的资源并且不要求硬件设备。
    不能将Arbiter部署在同一个数据集节点中，可以部署在其他应用服务器或者监视服务器中，也可部署在单独的虚拟机中。
    为了确保复制集中有奇数的投票成员（包括primary），需要添加仲裁节点做为投票，否则primary不能运行时不会自动切换primary。

简单了解之后，我们可以这样总结一下：
    应用请求mongos来操作mongodb的增删改查，配置服务器存储数据库元信息，并且和mongos做同步，数据最终存入在shard（分片）上。
    为了防止数据丢失同步在副本集中存储了一份，仲裁在数据存储到分片的时候决定存储到哪个节点。


=========================================================================================================

【环境准备】
系统系统 centos7
3台服务器：201/202/203
安装包： mongodb-linux-x86_64-rhel70-3.2.4.tgz

服务器规划
    192.168.1.201           192.168.1.202           192.168.1.203
    mongos	                mongos	                mongos
    config server	        config server	        config server
    shard server1 主节点 	shard server1 副节点 	shard server1 仲裁
    shard server2 仲裁 	    shard server2 主节点 	shard server2 副节点
    shard server3 副节点 	shard server3 仲裁	    shard server3 主节点
    
端口分配
    mongos：20000
    config：21000
    shard1：27001
    shard2：27002
    shard3：27003

                                192.168.1.201           192.168.1.202               192.168.1.203
mongo --port 20000              
mongo --port 21000              configs:SECONDARY>      configs:SECONDARY>          configs:PRIMARY>
mongo --port 27001              shard1:PRIMARY>         shard1:SECONDARY>           shard1:ARBITER>
mongo --port 27002              shard2:ARBITER>         shard2:PRIMARY>             shard2:SECONDARY>
mongo --port 27003              shard3:SECONDARY>         shard3:ARBITER>             shard3:PRIMARY>
    

    
浏览器访问；
http://192.168.1.201:28001/
http://192.168.1.201:28002/
http://192.168.1.201:28003/
    
====================================================================================
   
【集群搭建】
1、安装mongodb
#解压
tar -xzvf mongodb-linux-x86_64-rhel70-3.2.4.tgz -C /usr/local

#设置软连接
ln -s mongodb-linux-x86_64-rhel70-3.2.4 mongodb

# 规划目录
# 分别在每台机器建立conf、mongos、config、shard1、shard2、shard3六个目录，因为mongos不存储数据，只需要建立日志文件目录即可。

# 创建配置文件的统一存放目录（三台机器上执行）
mkdir -p /usr/local/mongodb/conf

# 创建数据目录、日志目录（三台机器上执行）
mkdir -p /usr/local/mongodb/config/{data,log}
mkdir -p /usr/local/mongodb/mongos/log
mkdir -p /usr/local/mongodb/shard1/{data,log}
mkdir -p /usr/local/mongodb/shard2/{data,log}
mkdir -p /usr/local/mongodb/shard3/{data,log}

# 配置环境变量（三台机器上执行）
vim /etc/profile.d/mongodb.sh

# 插入
export MONGODB_HOME=/usr/local/mongodb
export PATH=$MONGODB_HOME/bin:$PATH

# 使立即生效
source /etc/profile

----------------------------------------------------------------

2、config server配置服务器（三台机器上执行）
vi /usr/local/mongodb/conf/config.conf

----
pidfilepath = /usr/local/mongodb/config/log/configsrv.pid
dbpath = /usr/local/mongodb/config/data
logpath = /usr/local/mongodb/config/log/congigsrv.log
logappend = true
 
bind_ip = 0.0.0.0
port = 21000
fork = true
 
#declare this is a config db of a cluster;
configsvr = true

#副本集名称
replSet=configs
 
#设置最大连接数
maxConns=20000
----


启动三台服务器的config server
mongod -f /usr/local/mongodb/conf/config.conf


登录任意一台配置服务器，初始化配置副本集。
#连接
mongo --port 21000

# config变量
> config = {
            "_id" : "configs",
            "members" : [
                    {
                            "_id" : 0,
                            "host" : "192.168.1.201:21000"
                    },
                    {
                            "_id" : 1,
                            "host" : "192.168.1.202:21000"
                    },
                    {
                            "_id" : 2,
                            "host" : "192.168.1.203:21000"
                    }
            ]
        }
> rs.initiate(config)
{ "ok" : 1 }

# 其中，”_id” : “configs”应与配置文件中配置的 replicaction.replSetName 一致，”members” 中的 “host” 为三个节点的 ip 和 port

----------------------------------------------------------------

3、配置分片副本集（三台机器上执行）

## 设置第一个分片副本集

vi /usr/local/mongodb/conf/shard1.conf

----
pidfilepath = /usr/local/mongodb/shard1/log/shard1.pid
dbpath = /usr/local/mongodb/shard1/data
logpath = /usr/local/mongodb/shard1/log/shard1.log
logappend = true

bind_ip = 0.0.0.0
port = 27001
fork = true
 
#打开web监控
httpinterface=true
rest=true
 
#副本集名称
replSet=shard1
 
#declare this is a shard db of a cluster;
shardsvr = true
 
#设置最大连接数
maxConns=20000
----

启动三台服务器的shard1 server

mongod -f /usr/local/mongodb/conf/shard1.conf

登陆任意一台服务器，初始化副本集
mongo --port 27001

#使用admin数据库
use admin

#定义副本集配置，第三个节点的 "arbiterOnly":true 代表其为仲裁节点。
config = {
        "_id" : "shard1",
        "members" : [
                {
                        "_id" : 0,
                        "host" : "192.168.1.201:27001"
                },
                {
                        "_id" : 1,
                        "host" : "192.168.1.202:27001"
                },
                {
                        "_id" : 2,
                        "host" : "192.168.1.203:27001",
                        "arbiterOnly" : true
                }
        ]
}

#初始化副本集配置
> rs.initiate(config);
{ "ok" : 1 }
shard1:SECONDARY> 

----------------------------------------------------------------

## 设置第二个分片副本集
vi /usr/local/mongodb/conf/shard2.conf

----
pidfilepath = /usr/local/mongodb/shard2/log/shard2.pid
dbpath = /usr/local/mongodb/shard2/data
logpath = /usr/local/mongodb/shard2/log/shard2.log
logappend = true

bind_ip = 0.0.0.0
port = 27002
fork = true
 
#打开web监控
httpinterface=true
rest=true
 
#副本集名称
replSet=shard2
 
#declare this is a shard db of a cluster;
shardsvr = true
 
#设置最大连接数
maxConns=20000
----


启动三台服务器的shard2 server
mongod -f /usr/local/mongodb/conf/shard2.conf

登陆任意一台服务器，初始化副本集
mongo --port 27002

#使用admin数据库
use admin

#定义副本集配置
config = {
        "_id" : "shard2",
        "members" : [
                {
                        "_id" : 0,
                        "host" : "192.168.1.201:27002",
                        "arbiterOnly" : true
                },
                {
                        "_id" : 1,
                        "host" : "192.168.1.202:27002"
                },
                {
                        "_id" : 2,
                        "host" : "192.168.1.203:27002"
                }
        ]
}

#初始化副本集配置
> rs.initiate(config);
{ "ok" : 1 }


----------------------------------------------------------------

## 设置第三个分片副本集 
vi /usr/local/mongodb/conf/shard3.conf

---
pidfilepath = /usr/local/mongodb/shard3/log/shard3.pid
dbpath = /usr/local/mongodb/shard3/data
logpath = /usr/local/mongodb/shard3/log/shard3.log
logappend = true

bind_ip = 0.0.0.0
port = 27003
fork = true
 
#打开web监控
httpinterface=true
rest=true
 
#副本集名称
replSet=shard3
 
#declare this is a shard db of a cluster;
shardsvr = true
 
#设置最大连接数
maxConns=20000


启动三台服务器的shard3 server
mongod -f /usr/local/mongodb/conf/shard3.conf
---

启动三台服务器的shard3 server
mongod -f /usr/local/mongodb/conf/shard3.conf


登陆任意一台服务器，初始化副本集
mongo --port 27003

#使用admin数据库
use admin

#定义副本集配置
config = config = {
        "_id" : "shard3",
        "members" : [
                {
                        "_id" : 0,
                        "host" : "192.168.1.201:27003"
                },
                {
                        "_id" : 1,
                        "host" : "192.168.1.202:27003",
                        "arbiterOnly" : true
                },
                {
                        "_id" : 2,
                        "host" : "192.168.1.203:27003"
                }
        ]
}

#初始化副本集配置
> rs.initiate(config);
{ "ok" : 1 }


----------------------------------------------------------------

4、配置路由服务器 mongos（三台机器）

# 先启动配置服务器，再启动分片服务器, 最后启动路由实例:

vi /usr/local/mongodb/conf/mongos.conf

---
pidfilepath = /usr/local/mongodb/mongos/log/mongos.pid
logpath = /usr/local/mongodb/mongos/log/mongos.log
logappend = true

bind_ip = 0.0.0.0
port = 20000
fork = true

#监听的配置服务器,只能有1个或者3个 configs为配置服务器的副本集名字
configdb = configs/192.168.1.201:21000,192.168.1.202:21000,192.168.1.203:21000
 
#设置最大连接数
maxConns=20000

---

启动三台服务器的mongos server
mongos -f /usr/local/mongodb/conf/mongos.conf


----------------------------------------------------------------

5、启用分片

目前已经搭建了mongodb的配置服务器、路由服务器，以及3个分片服务器。
不过应用程序连接到mongos路由服务器并不能使用分片机制，还需要在程序里设置分片配置，让分片生效。

#登陆任意一台mongos
mongo --port 20000

#使用admin数据库
use  admin

#串联路由服务器与分配副本集
sh.addShard("shard1/192.168.1.201:27001,192.168.1.202:27001,192.168.1.203:27001");
sh.addShard("shard2/192.168.1.201:27002,192.168.1.202:27002,192.168.1.203:27002");
sh.addShard("shard3/192.168.1.201:27003,192.168.1.202:27003,192.168.1.203:27003");

#查看集群状态
sh.status()

--- Sharding Status --- 
  sharding version: {
        "_id" : 1,
        "minCompatibleVersion" : 5,
        "currentVersion" : 6,
        "clusterId" : ObjectId("5a3d1ddfb9221dcd320872f9")
}
  shards:
        {  "_id" : "shard1",  "host" : "shard1/192.168.1.201:27001,192.168.1.202:27001" }
        {  "_id" : "shard2",  "host" : "shard2/192.168.1.202:27002,192.168.1.203:27002" }
        {  "_id" : "shard3",  "host" : "shard3/192.168.1.201:27003,192.168.1.203:27003" }
  active mongoses:
        "3.2.4" : 3
  balancer:
        Currently enabled:  yes
        Currently running:  no
        Failed balancer rounds in last 5 attempts:  0
        Migration Results for the last 24 hours: 
                No recent migrations
  databases:


----------------------------------------------------------------  
  
  
6、测试
目前配置服务、路由服务、分片服务、副本集服务都已经串联起来了。
但我们的目的是希望插入数据，数据能够自动分片。连接在mongos上，准备让指定的数据库、指定的集合分片生效。

#登陆任意一台mongos
mongo --port 20000

#使用admin数据库
use  admin

#指定testdb分片生效
mongos> db.runCommand( { enablesharding: "testdb"});
{ "ok" : 1 }

#指定数据库里需要分片的集合和片键
#片键的指定：片键应该是离散分布的，这样可以在不同节点写入；要避免自增主键或时间戳单独做片键；大多数查询条件应该包含分片条件；
mongos> db.runCommand( { shardcollection: "testdb.users", key: {birth: 1} } )
{ "collectionsharded" : "testdb.users", "ok" : 1 }


设置testdb的 users 表需要分片，根据 id 自动分片到 shard1 ，shard2，shard3 这3个mongodb实例。
这样设置是因为不是所有mongodb的数据库和表都需要分片！



【测试分片】
#登陆任意一台mongos
mongo --port 20000

#使用testdb
mongos> use  testdb;

#插入10W条测试数据，正确的情况下，数据将被分片存储到3个不同的mongodb实例上。
function randomNum(Min, Max) {
	var Range = Max - Min;
	var Rand = Math.random();
	return (Min + Math.round(Rand * Range));
}

function getUser() {
	var user = {};
	user.birth = randomNum(1980,2017) + "-" + randomNum(1,12) + "-" + randomNum(1,30);
	user.name = "Alice" + randomNum(1,9999999);
	return user;
}

mongos> for (var i = 1; i <= 20000; i++) { db.users.save(getUser()) }
WriteResult({ "nInserted" : 1 })

#查看分片情况如下，部分无关信息省掉了
mongos> db.users.stats();
{
        "sharded" : true,
        "capped" : false,
        "ns" : "testdb.users",
        "count" : 20000,
        "indexSizes" : {
                "_id_" : 286720,
                "birth_1" : 282624
        },
        "avgObjSize" : 65.8147,
        "nindexes" : 2,
        "nchunks" : 3,
        "shards" : {
                "shard1" : {
                        "ns" : "testdb.users",
                        "count" : 744,
                        "size" : 48926,
                        ...
                        "ok" : 1
                },
                "shard2" : {
                        "ns" : "testdb.users",
                        "count" : 18165,
                        "size" : 1195472,
                        ...
                        "ok" : 1
                },
                "shard3" : {
                        "ns" : "testdb.users",
                        "count" : 1091,
                        "size" : 71896,
                        ...
                        "ok" : 1
                }
        },
        "ok" : 1
}
mongos> 


# 查看集群状态
mongos> sh.status()
--- Sharding Status --- 
  sharding version: {
        "_id" : 1,
        "minCompatibleVersion" : 5,
        "currentVersion" : 6,
        "clusterId" : ObjectId("5a3d1ddfb9221dcd320872f9")
}
  shards:
        {  "_id" : "shard1",  "host" : "shard1/192.168.1.201:27001,192.168.1.202:27001" }
        {  "_id" : "shard2",  "host" : "shard2/192.168.1.202:27002,192.168.1.203:27002" }
        {  "_id" : "shard3",  "host" : "shard3/192.168.1.201:27003,192.168.1.203:27003" }
  active mongoses:
        "3.2.4" : 3
  balancer:
        Currently enabled:  yes
        Currently running:  no
        Failed balancer rounds in last 5 attempts:  0
        Migration Results for the last 24 hours: 
                6 : Success
                2 : Failed with error 'aborted', from shard2 to shard1
  databases:
        {  "_id" : "testdb",  "primary" : "shard2",  "partitioned" : true }
                testdb.users
                        shard key: { "birth" : 1 }
                        unique: false
                        balancing: true
                        chunks:
                                shard1  1
                                shard2  1
                                shard3  1
                        { "birth" : { "$minKey" : 1 } } -->> { "birth" : "1982-4-2" } on : shard3 Timestamp(3, 0) 
                        { "birth" : "1982-4-2" } -->> { "birth" : "2016-1-4" } on : shard2 Timestamp(3, 1) 
                        { "birth" : "2016-1-4" } -->> { "birth" : { "$maxKey" : 1 } } on : shard1 Timestamp(2, 0)   

----------------------------------------------------------------  

【故障转移测试】

# 192.168.1.201 --- 201是shard3 的主节点
mongo --port 27003
shard3:PRIMARY> 


# 192.168.1.203 --- 203是shard3 的从节点
mongo --port 27003
shard3:SECONDARY>


# 192.168.1.201 --- 查询201上负责shard3的进程号
ps -ef | grep shard3
root      3786     1  2 01:43 ?        00:00:29 mongod -f /usr/local/mongodb/conf/shard3.conf

kill -9 3786    --- kill 掉 shard3 进程

mongo --port 27003  --- 201上节点已经无法连接shard3
exception: connect failed


# 192.168.1.203
shard3:PRIMARY>  --- 203节点自动被选举为 shard3 的新主节点，完成failover

# 192.168.1.201
mongod -f /usr/local/mongodb/conf/shard3.conf   --- 201上重新启动shard3的服务
mongo --port 27003 
shard3:SECONDARY> --- 201恢复之后，变为shard3 的从节点，到此，验证了分片集群对failover的支持！


---------------------------------------------------------------- 

【后期运维】

#启动
mongodb的启动顺序是，先启动配置服务器，在启动分片，最后启动mongos.

mongod -f /usr/local/mongodb/conf/config.conf

mongod -f /usr/local/mongodb/conf/shard1.conf
mongod -f /usr/local/mongodb/conf/shard2.conf
mongod -f /usr/local/mongodb/conf/shard3.conf

mongod -f /usr/local/mongodb/conf/mongos.conf

#关闭，直接killall杀掉所有进程
killall mongod
killall mongos

=========================================================================================================
【遇到的问题】
# 初始化分配副本集时报错
> rs.initiate(config);
{
        "ok" : 0,
        "errmsg" : "This node, 192.168.1.203:27001, with _id 2 is not electable under the new configuration version 1 for replica set shard1",
        "code" : 93
}

问题原因：
    当前登录客户端所在的节点被指定为了仲裁节点，不能在它上面进行设置。

解决办法；
    换到其它节点进行设置。
    

# 从节点默认没有开启读功能？
Error: error: { "ok" : 0, "errmsg" : "not master and slaveOk=false", "code" : 13435 }


