my.cnf是mysql启动时加载的配置文件，一般会放在mysql的安装目录中，用户也可以放在其他目录加载。

安装mysql后，系统中会有多个my.cnf文件，有些是用于测试的。

## 使用locate查找
使用locate my.cnf命令可以列出所有的my.cnf文件

命令: locate my.cnf

## 查看是否使用了指定目录的my.cnf
启动mysql后，我们查看mysql的进程，看看是否有设置使用指定目录的my.cnf文件，如果有则表示mysql启动时是加载了这个配置文件。

命令: ps aux|grep mysql|grep 'my.cnf'

如果上面的命令没有输出，表示没有设置使用指定目录的my.cnf。

## 查看mysql默认读取my.cnf的位置
如果没有设置使用指定目录的my.cnf，mysql启动时会读取安装目录根目录及默认目录下的my.cnf文件。

查看当前mysqld启动时的加载文件

命令: mysql --help|grep 'my.cnf'

```
[root@node1 mysql]# mysql --help|grep 'my.cnf'

                      order of preference, my.cnf, $MYSQL_TCP_PORT,

/etc/my.cnf /etc/mysql/my.cnf /usr/local/mysql/etc/my.cnf ~/.my.cnf 
```

/etc/my.cnf, /etc/mysql/my.cnf, /usr/local/etc/my.cnf, ~/.my.cnf 

这些就是mysql默认会搜寻my.cnf的目录，顺序排前的优先。


## 启动时没有使用配置文件
如果没有设置使用指定目录my.cnf文件及默认读取目录没有my.cnf文件，表示mysql启动时并没有加载配置文件，而是使用默认配置。

需要修改配置，可以在mysql默认读取的目录中，创建一个my.cnf文件(例如:/etc/my.cnf)，把需要修改的配置内容写入，重启mysql后即可生效。

## 启动时指定my.cnf
```
/usr/local/mysql/bin/mysqld --defaults-file=/etc/mysql/my.cnf --basedir=/usr --datadir=/var/lib/mysql --pid-file=/var/run/mysqld/mysqld.pid --socket=/var/run/mysqld/mysqld.sock
```
