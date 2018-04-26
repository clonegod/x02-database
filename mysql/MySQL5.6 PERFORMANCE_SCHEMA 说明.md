# MySQL5.6 PERFORMANCE_SCHEMA 说明

5.5开始新增一个数据库：PERFORMANCE_SCHEMA，主要用于收集数据库服务器性能参数。
并且库里表的存储引擎均为PERFORMANCE_SCHEMA，而用户是不能创建存储引擎为PERFORMANCE_SCHEMA的表。

MySQL5.5默认是关闭的，需要手动开启，在配置文件里添加：
	[mysqld]
	performance_schema=ON


从MySQL5.6开始，默认打开，本文就从MySQL5.6来说明，在数据库使用当中PERFORMANCE_SCHEMA的一些比较常用的功能。

##### 查看是否开启：
mysql>show variables like 'performance_schema';


#### 相关表信息
Performance Schema数据库的介绍，主要用于收集数据库服务器性能参数：

①提供进程等待的详细信息，包括锁、互斥变量、文件信息；

②保存历史的事件汇总信息，为提供MySQL服务器性能做出详细的判断；

③对于新增和删除监控事件点都非常容易，并可以改变mysql服务器的监控周期，例如（CYCLE、MICROSECOND）。通过该库得到数据库运行的统计信息，更好分析定位问题和完善监控信息。

##### 第一类：配置（setup）表

```
mysql> use performance_schema;

mysql> show tables like '%setup%';

+----------------------------------------+
| Tables_in_performance_schema (%setup%) |
+----------------------------------------+
| setup_actors                           |
| setup_consumers                        |
| setup_instruments                      |
| setup_objects                          |
| setup_timers                           |
+----------------------------------------+

```

###### performance_schema.setup_actors
配置用户维度的监控，默认监控所有用户。

```
mysql> select * from setup_actors;
+------+------+------+
| HOST | USER | ROLE |
+------+------+------+
| %    | %    | %    |
+------+------+------+
```

###### performance_schema.setup_consumers
配置events的消费者类型，即收集的events写入到哪些统计表中。

mysql> select * from setup_consumers;
+--------------------------------+---------+
| NAME                           | ENABLED |
+--------------------------------+---------+
| events_stages_current          | NO      |
| events_stages_history          | NO      |
| events_stages_history_long     | NO      |
| events_statements_current      | YES     |
| events_statements_history      | NO      |
| events_statements_history_long | NO      |
| events_waits_current           | NO      |
| events_waits_history           | NO      |
| events_waits_history_long      | NO      |
| global_instrumentation         | YES     |
| thread_instrumentation         | YES     |
| statements_digest              | YES     |
+--------------------------------+---------+

这里需要说明的是需要查看哪个就更新其ENABLED列为YES。

(**下面的一些例子是基于这些参数打开的情况下进行的，如果不启用，则无法对某些数据进行统计**)。

如：
```
mysql> update setup_consumers set ENABLED='YES' where NAME in ('events_stages_current','events_waits_current');
```

更新完后立即生效，但是服务器重启之后又会变回默认值，要永久生效需要在配置文件里添加：

	[mysqld]
	#performance_schema
	performance_schema_consumer_events_waits_current=on
	performance_schema_consumer_events_stages_current=on
	performance_schema_consumer_events_statements_current=on
	performance_schema_consumer_events_waits_history=on
	performance_schema_consumer_events_stages_history=on
	performance_schema_consumer_events_statements_history=on
	performance_schema_consumer_events_stages_history_long=on

###### performance_schema.setup_instruments
配置具体的instrument，主要包含4大类：idle、stage/xxx、statement/xxx、wait/xxx

```
mysql> select name,count(*) from setup_instruments group by LEFT(name,5);
+---------------------------------+----------+
| name                            | count(*) |
+---------------------------------+----------+
| idle                            |        1 |
| stage/sql/After create          |      108 |
| statement/sql/select            |      168 |
| wait/synch/mutex/sql/PAGE::lock |      279 |
+---------------------------------+----------+
```

	idle表示socket空闲的时间
	stage类表示语句的每个执行阶段的统计
	statement类统计语句维度的信息，
	wait类统计各种等待事件，比如IO，mutux，spin_lock,condition等。


###### performance_schema.setup_objects
配置监控对象，默认对mysql，performance_schema和information_schema中的表都不监控，而其它DB的所有表都监控。

```
mysql> select * from setup_objects;
+-------------+--------------------+-------------+---------+-------+
| OBJECT_TYPE | OBJECT_SCHEMA      | OBJECT_NAME | ENABLED | TIMED |
+-------------+--------------------+-------------+---------+-------+
| TABLE       | mysql              | %           | NO      | NO    |
| TABLE       | performance_schema | %           | NO      | NO    |
| TABLE       | information_schema | %           | NO      | NO    |
| TABLE       | %                  | %           | YES     | YES   |
+-------------+--------------------+-------------+---------+-------+
```

###### performance_schema.setup_timers
配置每种类型指令的统计时间单位。MICROSECOND表示统计单位是微妙，CYCLE表示统计单位是时钟周期，时间度量与CPU的主频有关，NANOSECOND表示统计单位是纳秒。但无论采用哪种度量单位，最终统计表中统计的时间都会装换到皮秒。（1秒＝1000000000000皮秒）

```
mysql> select * from setup_timers;
+-----------+-------------+
| NAME      | TIMER_NAME  |
+-----------+-------------+
| idle      | MICROSECOND |
| wait      | CYCLE       |
| stage     | NANOSECOND  |
| statement | NANOSECOND  |
+-----------+-------------+
```



##### 第2类：instance表

###### performance_schema.cond_instances 条件等待对象实例
表中记录了系统中使用的条件变量的对象，OBJECT_INSTANCE_BEGIN为对象的内存地址。

###### performance_schema.file_instances 文件实例
表中记录了系统中打开了文件的对象，包括ibdata文件，redo文件，binlog文件，用户的表文件等，open_count显示当前文件打开的数目，如果重来没有打开过，不会出现在表中。

```
mysql> select * from file_instances limit 2,5;
+---------------------------------------------------------------------+--------------------------------------+------------+
| FILE_NAME                                                           | EVENT_NAME                           | OPEN_COUNT |
+---------------------------------------------------------------------+--------------------------------------+------------+
| /usr/local/mysql-5.6.35-linux-glibc2.5-x86_64/data/mysql/plugin.frm | wait/io/file/sql/FRM                 |          0 |
| /usr/local/mysql-5.6.35-linux-glibc2.5-x86_64/data/mysql/plugin.MYI | wait/io/file/myisam/kfile            |          0 |
| /usr/local/mysql-5.6.35-linux-glibc2.5-x86_64/data/mysql/plugin.MYD | wait/io/file/myisam/dfile            |          0 |
| /usr/local/mysql-5.6.35-linux-glibc2.5-x86_64/data/ibdata1          | wait/io/file/innodb/innodb_data_file |          2 |
| /usr/local/mysql-5.6.35-linux-glibc2.5-x86_64/data/ib_logfile0      | wait/io/file/innodb/innodb_log_file  |          2 |
+---------------------------------------------------------------------+--------------------------------------+------------+
```


###### performance_schema.mutex_instances 互斥同步对象实例
表中记录了系统中使用互斥量对象的所有记录，其中name为：wait/synch/mutex/*。

**LOCKED_BY_THREAD_ID**显示哪个线程正持有mutex，若没有线程持有，则为NULL。

###### performance_schema.rwlock_instances 读写锁同步对象实例
表中记录了系统中使用读写锁对象的所有记录，其中name为 wait/synch/rwlock/*。

WRITE_LOCKED_BY_THREAD_ID为正在持有该对象的thread_id，若没有线程持有，则为NULL。

READ_LOCKED_BY_COUNT为记录了同时有多少个读者持有读锁。

（通过 events_waits_current 表可以知道，哪个线程在等待锁；

通过rwlock_instances知道哪个线程持有锁。

rwlock_instances的缺陷是，只能记录持有写锁的线程，对于读锁则无能为力）。


###### performance_schema.socket_instances 活跃会话对象实例
表中记录了thread_id,socket_id,ip和port，其它表可以通过thread_id与socket_instance进行关联，获取IP-PORT信息，能够与应用对接起来。

event_name主要包含3类：

	wait/io/socket/sql/server_unix_socket，服务端unix监听socket
	wait/io/socket/sql/server_tcpip_socket，服务端tcp监听socket
	wait/io/socket/sql/client_connection，客户端socket


##### 第3类：wait表

###### performance_schema.events_waits_current 记录了当前线程等待的事件

###### performance_schema.events_waits_history 记录了每个线程最近等待的10个事件

###### performance_schema.events_waits_history_long 记录了最近所有线程产生的10000个事件

```
CREATE TABLE `events_waits_current` (
`THREAD_ID` bigint(20) unsigned NOT NULL COMMENT '线程ID',
`EVENT_ID` bigint(20) unsigned NOT NULL COMMENT '当前线程的事件ID，和THREAD_ID确定唯一',
`END_EVENT_ID` bigint(20) unsigned DEFAULT NULL COMMENT '当事件开始时，这一列被设置为NULL。当事件结束时，再更新为当前的事件ID',
`EVENT_NAME` varchar(128) NOT NULL COMMENT '事件名称',
`SOURCE` varchar(64) DEFAULT NULL COMMENT '该事件产生时的源码文件',
`TIMER_START` bigint(20) unsigned DEFAULT NULL COMMENT '事件开始时间（皮秒）',
`TIMER_END` bigint(20) unsigned DEFAULT NULL COMMENT '事件结束结束时间（皮秒）',
`TIMER_WAIT` bigint(20) unsigned DEFAULT NULL COMMENT '事件等待时间（皮秒）',
`SPINS` int(10) unsigned DEFAULT NULL COMMENT '',
`OBJECT_SCHEMA` varchar(64) DEFAULT NULL COMMENT '库名',
`OBJECT_NAME` varchar(512) DEFAULT NULL COMMENT '文件名、表名、IP:SOCK值',
`OBJECT_TYPE` varchar(64) DEFAULT NULL COMMENT 'FILE、TABLE、TEMPORARY TABLE',
`INDEX_NAME` varchar(64) DEFAULT NULL COMMENT '索引名',
`OBJECT_INSTANCE_BEGIN` bigint(20) unsigned NOT NULL COMMENT '内存地址',
`NESTING_EVENT_ID` bigint(20) unsigned DEFAULT NULL COMMENT '该事件对应的父事件ID',
`NESTING_EVENT_TYPE` enum('STATEMENT','STAGE','WAIT') DEFAULT NULL COMMENT '父事件类型(STATEMENT, STAGE, WAIT)',
`OPERATION` varchar(32) NOT NULL COMMENT '操作类型（lock, read, write）',
`NUMBER_OF_BYTES` bigint(20) DEFAULT NULL COMMENT '',
`FLAGS` int(10) unsigned DEFAULT NULL COMMENT '标记'
) ENGINE=PERFORMANCE_SCHEMA DEFAULT CHARSET=utf8
```

##### 第4类：stage 表 

###### performance_schema.events_stages_current 记录了当前线程所处的执行阶段

###### performance_schema.events_stages_history 记录了当前线程所处的执行阶段10条历史记录

###### performance_schema.events_stages_history_long 记录了当前线程所处的执行阶段10000条历史记录

```
CREATE TABLE `events_stages_current` (
`THREAD_ID` bigint(20) unsigned NOT NULL COMMENT '线程ID',
`EVENT_ID` bigint(20) unsigned NOT NULL COMMENT '事件ID',
`END_EVENT_ID` bigint(20) unsigned DEFAULT NULL COMMENT '结束事件ID',
`EVENT_NAME` varchar(128) NOT NULL COMMENT '事件名称',
`SOURCE` varchar(64) DEFAULT NULL COMMENT '源码位置',
`TIMER_START` bigint(20) unsigned DEFAULT NULL COMMENT '事件开始时间（皮秒）',
`TIMER_END` bigint(20) unsigned DEFAULT NULL COMMENT '事件结束结束时间（皮秒）',
`TIMER_WAIT` bigint(20) unsigned DEFAULT NULL COMMENT '事件等待时间（皮秒）',
`NESTING_EVENT_ID` bigint(20) unsigned DEFAULT NULL COMMENT '该事件对应的父事件ID',
`NESTING_EVENT_TYPE` enum('STATEMENT','STAGE','WAIT') DEFAULT NULL COMMENT '父事件类型(STATEMENT, STAGE, WAIT)'
) ENGINE=PERFORMANCE_SCHEMA DEFAULT CHARSET=utf8
```

##### 第5类：statement 表

###### performance_schema.events_statements_current
通过 thread_id+event_id可以唯一确定一条记录。Statments表只记录最顶层的请求，SQL语句或是COMMAND，每条语句一行。

event_name形式为statement/sql/*，或statement/com/*

###### performance_schema.events_statements_history

###### performance_schema.events_statements_history_long

```
CREATE TABLE `events_statements_current` (
`THREAD_ID` bigint(20) unsigned NOT NULL COMMENT '线程ID',
`EVENT_ID` bigint(20) unsigned NOT NULL COMMENT '事件ID',
`END_EVENT_ID` bigint(20) unsigned DEFAULT NULL COMMENT '结束事件ID',
`EVENT_NAME` varchar(128) NOT NULL COMMENT '事件名称',
`SOURCE` varchar(64) DEFAULT NULL COMMENT '源码位置',
`TIMER_START` bigint(20) unsigned DEFAULT NULL COMMENT '事件开始时间（皮秒）',
`TIMER_END` bigint(20) unsigned DEFAULT NULL COMMENT '事件结束结束时间（皮秒）',
`TIMER_WAIT` bigint(20) unsigned DEFAULT NULL COMMENT '事件等待时间（皮秒）',
`LOCK_TIME` bigint(20) unsigned NOT NULL COMMENT '锁时间',
`SQL_TEXT` longtext COMMENT '记录SQL语句',
`DIGEST` varchar(32) DEFAULT NULL COMMENT '对SQL_TEXT做MD5产生的32位字符串',
`DIGEST_TEXT` longtext COMMENT '将语句中值部分用问号代替，用于SQL语句归类',
`CURRENT_SCHEMA` varchar(64) DEFAULT NULL COMMENT '默认的数据库名',
`OBJECT_TYPE` varchar(64) DEFAULT NULL COMMENT '保留字段',
`OBJECT_SCHEMA` varchar(64) DEFAULT NULL COMMENT '保留字段',
`OBJECT_NAME` varchar(64) DEFAULT NULL COMMENT '保留字段',
`OBJECT_INSTANCE_BEGIN` bigint(20) unsigned DEFAULT NULL COMMENT '内存地址',
`MYSQL_ERRNO` int(11) DEFAULT NULL COMMENT '',
`RETURNED_SQLSTATE` varchar(5) DEFAULT NULL COMMENT '',
`MESSAGE_TEXT` varchar(128) DEFAULT NULL COMMENT '信息',
`ERRORS` bigint(20) unsigned NOT NULL COMMENT '错误数目',
`WARNINGS` bigint(20) unsigned NOT NULL COMMENT '警告数目',
`ROWS_AFFECTED` bigint(20) unsigned NOT NULL COMMENT '影响的数目',
`ROWS_SENT` bigint(20) unsigned NOT NULL COMMENT '返回的记录数',
`ROWS_EXAMINED` bigint(20) unsigned NOT NULL COMMENT '读取扫描的记录数目',
`CREATED_TMP_DISK_TABLES` bigint(20) unsigned NOT NULL COMMENT '创建磁盘临时表数目',
`CREATED_TMP_TABLES` bigint(20) unsigned NOT NULL COMMENT '创建临时表数目',
`SELECT_FULL_JOIN` bigint(20) unsigned NOT NULL COMMENT 'join时，第一个表为全表扫描的数目',
`SELECT_FULL_RANGE_JOIN` bigint(20) unsigned NOT NULL COMMENT '引用表采用range方式扫描的数目',
`SELECT_RANGE` bigint(20) unsigned NOT NULL COMMENT 'join时，第一个表采用range方式扫描的数目',
`SELECT_RANGE_CHECK` bigint(20) unsigned NOT NULL COMMENT '',
`SELECT_SCAN` bigint(20) unsigned NOT NULL COMMENT 'join时，第一个表位全表扫描的数目',
`SORT_MERGE_PASSES` bigint(20) unsigned NOT NULL COMMENT '',
`SORT_RANGE` bigint(20) unsigned NOT NULL COMMENT '范围排序数目',
`SORT_ROWS` bigint(20) unsigned NOT NULL COMMENT '排序的记录数目',
`SORT_SCAN` bigint(20) unsigned NOT NULL COMMENT '全表排序数目',
`NO_INDEX_USED` bigint(20) unsigned NOT NULL COMMENT '没有使用索引数目',
`NO_GOOD_INDEX_USED` bigint(20) unsigned NOT NULL COMMENT '',
`NESTING_EVENT_ID` bigint(20) unsigned DEFAULT NULL COMMENT '该事件对应的父事件ID',
`NESTING_EVENT_TYPE` enum('STATEMENT','STAGE','WAIT') DEFAULT NULL COMMENT '父事件类型(STATEMENT, STAGE, WAIT)'
) ENGINE=PERFORMANCE_SCHEMA DEFAULT CHARSET=utf8
```


##### 第6类：connection 表
###### performance_schema.users 记录用户连接数信息
```
mysql> select * from users;
+-------+---------------------+-------------------+
| USER  | CURRENT_CONNECTIONS | TOTAL_CONNECTIONS |
+-------+---------------------+-------------------+
| alice |                   1 |                 1 |
| root  |                   3 |                 3 |
| NULL  |                  18 |                20 |
+-------+---------------------+-------------------+
```

###### performance_schema.hosts 记录了主机连接数信息
```

mysql> select * from hosts;
+---------------+---------------------+-------------------+
| HOST          | CURRENT_CONNECTIONS | TOTAL_CONNECTIONS |
+---------------+---------------------+-------------------+
| localhost     |                   3 |                 3 |
| NULL          |                  18 |                20 |
| 192.168.1.103 |                   1 |                 1 |
+---------------+---------------------+-------------------+
```


###### performance_schema.accounts 记录了用户主机连接数信息
```
mysql> select * from accounts;
+-------+---------------+---------------------+-------------------+
| USER  | HOST          | CURRENT_CONNECTIONS | TOTAL_CONNECTIONS |
+-------+---------------+---------------------+-------------------+
| root  | localhost     |                   3 |                 3 |
| alice | 192.168.1.103 |                   1 |                 1 |
| NULL  | NULL          |                  18 |                20 |
+-------+---------------+---------------------+-------------------+
```

##### 第7类：summary 表

Summary表聚集了各个维度的统计信息包括表维度，索引维度，会话维度，语句维度和锁维度的统计信息。

###### performance_schema.events_waits_summary_global_by_event_name
按等待事件类型聚合，每个事件一条记录


###### performance_schema.events_waits_summary_by_instance
按等待事件对象聚合，同一种等待事件，可能有多个实例，每个实例有不同的内存地址，

因此event_name+object_instance_begin唯一确定一条记录。



###### performance_schema.events_waits_summary_by_thread_by_event_name
按每个线程和事件来统计，thread_id+event_name唯一确定一条记录。


###### performance_schema.events_stages_summary_global_by_event_name
按事件阶段类型聚合，每个事件一条记录，表结构同上。

###### performance_schema.events_stages_summary_by_thread_by_event_name
按每个线程和事件来阶段统计，表结构同上。


###### performance_schema.events_statements_summary_by_digest
按照事件的语句进行聚合。
```
CREATE TABLE `events_statements_summary_by_digest` (
`SCHEMA_NAME` varchar(64) DEFAULT NULL COMMENT '库名',
`DIGEST` varchar(32) DEFAULT NULL COMMENT '对SQL_TEXT做MD5产生的32位字符串。如果为consumer表中没有打开statement_digest选项，则为NULL',
`DIGEST_TEXT` longtext COMMENT '将语句中值部分用问号代替，用于SQL语句归类。如果为consumer表中没有打开statement_digest选项，则为NULL。',
`COUNT_STAR` bigint(20) unsigned NOT NULL COMMENT '事件计数',
`SUM_TIMER_WAIT` bigint(20) unsigned NOT NULL COMMENT '总的等待时间',
`MIN_TIMER_WAIT` bigint(20) unsigned NOT NULL COMMENT '最小等待时间',
`AVG_TIMER_WAIT` bigint(20) unsigned NOT NULL COMMENT '平均等待时间',
`MAX_TIMER_WAIT` bigint(20) unsigned NOT NULL COMMENT '最大等待时间',
`SUM_LOCK_TIME` bigint(20) unsigned NOT NULL COMMENT '锁时间总时长',
`SUM_ERRORS` bigint(20) unsigned NOT NULL COMMENT '错误数的总',
`SUM_WARNINGS` bigint(20) unsigned NOT NULL COMMENT '警告的总数',
`SUM_ROWS_AFFECTED` bigint(20) unsigned NOT NULL COMMENT '影响的总数目',
`SUM_ROWS_SENT` bigint(20) unsigned NOT NULL COMMENT '返回总数目',
`SUM_ROWS_EXAMINED` bigint(20) unsigned NOT NULL COMMENT '总的扫描的数目',
`SUM_CREATED_TMP_DISK_TABLES` bigint(20) unsigned NOT NULL COMMENT '创建磁盘临时表的总数目',
`SUM_CREATED_TMP_TABLES` bigint(20) unsigned NOT NULL COMMENT '创建临时表的总数目',
`SUM_SELECT_FULL_JOIN` bigint(20) unsigned NOT NULL COMMENT '第一个表全表扫描的总数目',
`SUM_SELECT_FULL_RANGE_JOIN` bigint(20) unsigned NOT NULL COMMENT '总的采用range方式扫描的数目',
`SUM_SELECT_RANGE` bigint(20) unsigned NOT NULL COMMENT '第一个表采用range方式扫描的总数目',
`SUM_SELECT_RANGE_CHECK` bigint(20) unsigned NOT NULL COMMENT '',
`SUM_SELECT_SCAN` bigint(20) unsigned NOT NULL COMMENT '第一个表位全表扫描的总数目',
`SUM_SORT_MERGE_PASSES` bigint(20) unsigned NOT NULL COMMENT '',
`SUM_SORT_RANGE` bigint(20) unsigned NOT NULL COMMENT '范围排序总数',
`SUM_SORT_ROWS` bigint(20) unsigned NOT NULL COMMENT '排序的记录总数目',
`SUM_SORT_SCAN` bigint(20) unsigned NOT NULL COMMENT '第一个表排序扫描总数目',
`SUM_NO_INDEX_USED` bigint(20) unsigned NOT NULL COMMENT '没有使用索引总数',
`SUM_NO_GOOD_INDEX_USED` bigint(20) unsigned NOT NULL COMMENT '',
`FIRST_SEEN` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '第一次执行时间',
`LAST_SEEN` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '最后一次执行时间'
) ENGINE=PERFORMANCE_SCHEMA DEFAULT CHARSET=utf8
```


###### performance_schema.events_statements_summary_global_by_event_name
按照事件的语句进行聚合。表结构同上。


###### performance_schema.events_statements_summary_by_thread_by_event_name
按照线程和事件的语句进行聚合，表结构同上。

###### performance_schema.file_summary_by_instance
按事件类型统计（物理IO维度）

###### performance_schema.file_summary_by_event_name
具体文件统计（物理IO维度）

###### performance_schema.table_io_waits_summary_by_table
根据wait/io/table/sql/handler，聚合每个表的I/O操作（逻辑IO纬度）

###### performance_schema.table_io_waits_summary_by_index_usage
与table_io_waits_summary_by_table类似，按索引维度统计

###### performance_schema.table_lock_waits_summary_by_table
聚合了表锁等待事件

###### performance_schema.socket_summary_by_instance
socket聚合统计表。

###### performance_schema.socket_summary_by_event_name
socket聚合统计表。

##### 第8类：其他相关表
###### performance_schema.performance_timers
系统支持的统计时间单位

###### performance_schema.threads
监视服务端的当前运行的线程


----------------------------
关于SQL维度的统计信息主要集中在events_statements_summary_by_digest表中，

通过将SQL语句抽象出digest，可以统计某类SQL语句在各个维度的统计信息。

#### 哪个SQL执行最多

```
mysql> SELECT SCHEMA_NAME,DIGEST_TEXT,COUNT_STAR,SUM_ROWS_SENT,SUM_ROWS_EXAMINED,FIRST_SEEN,LAST_SEEN FROM events_statements_summary_by_digest ORDER BY COUNT_STAR DESC LIMIT 1\G

*************************** 1. row ***************************
      SCHEMA_NAME: test
      DIGEST_TEXT: SELECT * FROM `t_user` WHERE NAME = ? 
       COUNT_STAR: 61
    SUM_ROWS_SENT: 0
SUM_ROWS_EXAMINED: 0
       FIRST_SEEN: 2018-01-12 01:55:28
        LAST_SEEN: 2018-01-12 01:55:56
1 row in set (0.00 sec)
```

各个字段的注释可以看上面的表结构说明：

从2018-01-12 01:55:28到2018-01-12 01:55:56该SQL执行了61次。


#### 哪个SQL平均响应时间最多
```
mysql> SELECT SCHEMA_NAME,DIGEST_TEXT,COUNT_STAR,AVG_TIMER_WAIT,SUM_ROWS_SENT,SUM_ROWS_EXAMINED,FIRST_SEEN,LAST_SEEN FROM events_statements_summary_by_digest ORDER BY AVG_TIMER_WAIT DESC LIMIT 1\G

*************************** 1. row ***************************
      SCHEMA_NAME: test
      DIGEST_TEXT: UPDATE `t_user` SET `age` = ? WHERE `id` = ? 
       COUNT_STAR: 5
   AVG_TIMER_WAIT: 20423823548000
    SUM_ROWS_SENT: 0
SUM_ROWS_EXAMINED: 3
       FIRST_SEEN: 2018-01-12 00:47:00
        LAST_SEEN: 2018-01-12 00:57:35
1 row in set (0.00 sec)
```

各个字段的注释可以看上面的表结构说明：

从2018-01-12 00:47:00到2018-01-12 00:57:35该SQL平均响应时间20423823548000皮秒（1000000000000皮秒=1秒）


#### 哪个SQL扫描的行数最多
SUM_ROWS_EXAMINED

#### 哪个SQL使用的临时表最多：
SUM_CREATED_TMP_DISK_TABLES、SUM_CREATED_TMP_TABLES

#### 哪个SQL返回的结果集最多：
SUM_ROWS_SENT

#### 哪个SQL排序数最多：
SUM_SORT_ROWS


通过上述指标我们可以间接获得某类SQL的逻辑IO(SUM_ROWS_EXAMINED)，CPU消耗(SUM_SORT_ROWS)，网络带宽(SUM_ROWS_SENT)的对比。

通过file_summary_by_instance表，可以获得系统运行到现在，哪个文件(表)物理IO最多，这可能意味着这个表经常需要访问磁盘IO

#### 哪个表、文件逻辑IO最多（热数据）：
```
mysql> SELECT FILE_NAME,EVENT_NAME,COUNT_READ,SUM_NUMBER_OF_BYTES_READ,COUNT_WRITE,SUM_NUMBER_OF_BYTES_WRITE FROM file_summary_by_instance ORDER BY SUM_NUMBER_OF_BYTES_READ+SUM_NUMBER_OF_BYTES_WRITE DESC LIMIT 1\G

*************************** 1. row ***************************
                FILE_NAME: /usr/local/mysql-5.6.35-linux-glibc2.5-x86_64/data/ibdata1
               EVENT_NAME: wait/io/file/innodb/innodb_data_file
               COUNT_READ: 995
 SUM_NUMBER_OF_BYTES_READ: 18366464
              COUNT_WRITE: 8
SUM_NUMBER_OF_BYTES_WRITE: 180224
```


#### 哪个索引使用最多：
```
mysql> SELECT OBJECT_NAME, INDEX_NAME, COUNT_FETCH, COUNT_INSERT, COUNT_UPDATE, COUNT_DELETE FROM table_io_waits_summary_by_index_usage ORDER BY SUM_TIMER_WAIT DESC limit 1;

+-------------+------------+-------------+--------------+--------------+--------------+
| OBJECT_NAME | INDEX_NAME | COUNT_FETCH | COUNT_INSERT | COUNT_UPDATE | COUNT_DELETE |
+-------------+------------+-------------+--------------+--------------+--------------+
| t_user      | PRIMARY    |           5 |            0 |            3 |            0 |
+-------------+------------+-------------+--------------+--------------+--------------+
```

通过table_io_waits_summary_by_index_usage表，可以获得系统运行到现在，哪个表的具体哪个索引(包括主键索引，二级索引)使用最多。

#### 哪个表有索引，但从没有使用过：
```
mysql> SELECT OBJECT_SCHEMA, OBJECT_NAME, INDEX_NAME FROM table_io_waits_summary_by_index_usage WHERE INDEX_NAME IS NOT NULL AND COUNT_STAR = 0 AND OBJECT_SCHEMA <> 'mysql' ORDER BY OBJECT_SCHEMA,OBJECT_NAME DESC LIMIT 1;

+---------------+-------------+------------+
| OBJECT_SCHEMA | OBJECT_NAME | INDEX_NAME |
+---------------+-------------+------------+
| test          | t_order_ext | order_id   |
+---------------+-------------+------------+
```

查询结果表明：test库的t_order_ext表中order_id索引没有被使用过。


#### 哪个等待事件消耗的时间最多：

```
mysql> SELECT EVENT_NAME, COUNT_STAR, SUM_TIMER_WAIT, AVG_TIMER_WAIT FROM events_waits_summary_global_by_event_name WHERE event_name != 'idle'ORDER BY SUM_TIMER_WAIT DESC LIMIT 1;

+---------------------------+------------+-----------------+----------------+
| EVENT_NAME                | COUNT_STAR | SUM_TIMER_WAIT  | AVG_TIMER_WAIT |
+---------------------------+------------+-----------------+----------------+
| wait/io/table/sql/handler |       1794 | 102576784512510 |    57177694647 |
+---------------------------+------------+-----------------+----------------+
```



##### 比如分析包含count(*)的某条SQL语句，具体如下：
```

mysql> SELECT  EVENT_ID,  sql_text  FROM events_statements_history  WHERE sql_text LIKE '%count(*)%' ORDER BY EVENT_ID DESC LIMIT 1;
+----------+--------------------------------------------------+
| EVENT_ID | sql_text                                         |
+----------+--------------------------------------------------+
|       52 | select count(*) from test_mybatis_blog.`comment` |
+----------+--------------------------------------------------+
```

首先得到了语句的event_id为381，通过查找events_stages_xxx中nesting_event_id为381的记录，可以达到目的。

