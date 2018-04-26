
### 测试表

插入10W条数据到t_order，可使用jdbcTemplate进行批量插入，每次插入1W条，分10次插入。
```
CREATE TABLE `t_order` (
	`order_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
	`express_type` TINYINT(1) UNSIGNED NOT NULL COMMENT '快递方式',
	`user_id` INT(10) UNSIGNED NULL DEFAULT NULL COMMENT '用户ID',
	`add_time` INT(10) NOT NULL COMMENT '下单时间',
	PRIMARY KEY (`order_id`),
	INDEX `user_id` (`user_id`),
	INDEX `express_type` (`express_type`)
)
COMMENT='订单记录表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
```

下面是订单的扩展表，仅向其中插入了一条记录，关联某笔订单。
```
CREATE TABLE `t_order_ext` (
	`order_id` INT(10) NOT NULL COMMENT '订单ID',
	`user_type` INT(11) NOT NULL DEFAULT '0' COMMENT '用户类型',
	`comment` VARCHAR(255) NOT NULL COMMENT '订单备注',
	INDEX `order_id` (`order_id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

```

----------------------------------------------------------

MySQL Key值（PRI, UNI, MUL）的含义

	PRI主键约束；
	UNI唯一约束；
	MUL可以重复；

先看一下第一种用法，与describe 是等价的：

##### mysql> describe t_order;

##### mysql> explain t_order; 
 
Field 	| Type |	Null |	Key |	Default |	Extra
------ | ------|------| ------|------| -------
order_id | int(10) unsigned | NO | PRI | (NULL) | auto_increment
express_type | tinyint(1) unsigned | NO | MUL | (NULL) |
user_id | int(10) unsigned | YES | MUL | (NULL) |
add_time | int(10) | NO | | (NULL) |



重点是第二种用法，需要深入的了解。

先看一个例子：

##### mysql> explain select * from t_order; 

id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra
------ | ------|------| ------|------| -------| ------|------| ------|------|
1 | SIMPLE | t_order | ALL | (NULL) | (NULL) | (NULL) | (NULL) | 100187 | (NULL)


加上extended后之后，多出1列(filtered)：

##### mysql> explain extended select * from t_order; 

id | select_type | table | type | possible_keys | key | key_len | ref | rows | filterd |  Extra
------ | ------|------| ------|------| -------| ------|------| ------|------|------
1 | SIMPLE | t_order | ALL | (NULL) | (NULL) | (NULL) | (NULL) | 100187 | 100.00 | (NULL)


----------------------------------------------------------------

下面对几个重要的参数进行一下说明

## select_type的说明

###### 1、select_type -> UNION
当通过union来连接多个查询结果时，第二个之后的select其select_type为UNION。

```
EXPLAIN
SELECT * FROM t_order WHERE order_id=100 
UNION
SELECT * FROM t_order WHERE order_id=200;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | PRIMARY | t_order | const | PRIMARY | PRIMARY | 4 | const | 1 | NULL | 
| 2 | UNION | t_order | const | PRIMARY | PRIMARY | 4 | const | 1 | NULL | 
| NULL | UNION RESULT | <union1,2> | ALL | NULL | NULL | NULL | NULL | NULL | Using temporary | 



###### 2、select_type -> DEPENDENT UNION与DEPENDENT SUBQUERY
当union作为子查询时，其中第二个union的select_type就是DEPENDENT UNION。

第一个子查询的select_type则是DEPENDENT SUBQUERY。

```
EXPLAIN
SELECT * FROM t_order 
WHERE order_id IN (
	SELECT order_id
		FROM t_order
		WHERE order_id=100 
	UNION
	SELECT order_id
		FROM t_order
		WHERE order_id=200
);
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | PRIMARY | t_order | ALL | NULL | NULL | NULL | NULL | 100187 | Using where | 
| 2 | DEPENDENT SUBQUERY | t_order | const | PRIMARY | PRIMARY | 4 | const | 1 | Using index | 
| 3 | DEPENDENT UNION | t_order | const | PRIMARY | PRIMARY | 4 | const | 1 | Using index | 
| NULL | UNION RESULT | <union2,3> | ALL | NULL | NULL | NULL | NULL | NULL | Using temporary | 



###### 3、select_type -> SUBQUERY
子查询中的第一个select其select_type为SUBQUERY。

```
EXPLAIN
SELECT * 
FROM t_order
WHERE order_id=(
	SELECT order_id FROM t_order WHERE order_id=100
);
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | PRIMARY | t_order | const | PRIMARY | PRIMARY | 4 | const | 1 | NULL | 
| 2 | SUBQUERY | t_order | const | PRIMARY | PRIMARY | 4 | const | 1 | Using index | 



###### 4、select_type -> DERIVED
当子查询是from子句时，其select_type为DERIVED。

```
EXPLAIN
SELECT * FROM (
	SELECT order_id FROM t_order WHERE order_id=100
) tmp;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | PRIMARY |  derived2 | system | NULL | NULL | NULL | NULL | 1 | NULL | 
| 2 | DERIVED | t_order | const | PRIMARY | PRIMARY | 4 | const | 1 | Using index | 


## type的说明
###### 1、 type -> system，const
见上面4.DERIVED的例子。其中第一行的type就是为system，第二行是const，这两种联接类型是最快的。

###### 2、 type -> eq_ref
在t_order表中的order_id是主键，t_order_ext表中的order_id也是主键，该表可以认为是订单表的补充信息表，他们的关系是1对1。

在下面的例子中可以看到a表的连接类型是eq_ref，这是极快的联接类型。

```
EXPLAIN
SELECT * 
FROM t_order a, t_order_ext b
WHERE a.order_id = b.order_id;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | b | ALL | order_id | NULL | NULL | NULL | 1 | NULL | 
| 1 | SIMPLE | a | eq_ref | PRIMARY | PRIMARY | 4 | test.b.order_id | 1 | Using where | 


###### 3、 type -> ref
下面的例子在上面的例子上略作了修改，加上了条件。此时b表的联接类型变成了ref。

因为所有与a表中order_id=100的匹配记录都将会从b表获取。这是比较常见的联接类型。

```
EXPLAIN
SELECT *
FROM t_order a, t_order_ext b
WHERE a.order_id = b.order_id AND a.order_id = 100;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | a | const | PRIMARY | PRIMARY | 4 | const | 1 | NULL | 
| 1 | SIMPLE | b | ref | order_id | order_id | 4 | const | 1 | NULL | 


###### 4、 type -> ref_or_null
user_id字段是一个可以为空的字段，并对该字段创建了一个索引。在下面的查询中可以看到联接类型为ref_or_null，这是mysql为含有null的字段专门做的处理。

在表设计中应当尽量避免索引字段为NULL，因为这会额外的耗费mysql的处理时间来做优化。

```
EXPLAIN
SELECT *
FROM t_order
WHERE user_id=100 OR user_id IS NULL;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | t_order | ref_or_null | user_id | user_id | 5 | const | 2 | Using index condition | 


###### 5、 type -> index_merge
经常出现在使用一张表中的多个索引时。mysql会将多个索引合并在一起，如下例:

```
EXPLAIN
SELECT *
FROM t_order
WHERE order_id=100 OR user_id=10;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | t_order | index_merge | PRIMARY,user_id | PRIMARY,user_id | 4,5 | NULL | 2 | Using union(PRIMARY,user_id); Using where | 


###### 6、 type -> unique_subquery  


###### 7、 type -> index_subquery


###### 8、 type -> range
按指定的范围进行检索，很常见。

```
EXPLAIN
SELECT *
FROM t_order
WHERE user_id IN (100,200,300);
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | t_order | range | user_id | user_id | 5 | NULL | 3 | Using index condition | 


###### 9、 type -> index
在进行统计时非常常见，此联接类型实际上会扫描索引树，仅比ALL快些。

```
EXPLAIN
SELECT COUNT(*)
FROM t_order;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | t_order | index | NULL | express_type | 1 | NULL | 100187 | Using index | 


###### 10、 type -> ALL
完整的扫描全表，最慢的联接类型，尽可能的避免。

where条件中的add_time列没有建立索引。

```
EXPLAIN
SELECT *
FROM t_order
WHERE add_time > 100;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | t_order | ALL | NULL | NULL | NULL | NULL | 100187 | Using where | 


## extra的说明
###### 1、 extra -> Distinct
MySQL发现第1个匹配行后,停止为当前的行组合搜索更多的行。

###### 2、 extra -> Not exists
因为b表中的order_id设置了NotNULL，所以mysql在用a表的order_id扫描t_order表，并查找b表的行时，如果在b表发现一个匹配的行就不再继续扫描b了，因为b表中的order_id字段不可能为NULL。这样避免了对b表的多次扫描。

```
EXPLAIN
SELECT COUNT(1)
FROM t_order a
LEFT JOIN t_order_ext b ON a.order_id=b.order_id
WHERE b.order_id IS NULL;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | a | index | NULL | express_type | 1 | NULL | 100187 | Using index | 
| 1 | SIMPLE | b | ref | order_id | order_id | 4 | test.a.order_id | 1 | Using where; Not exists; Using index | 

###### 3、 extra -> Using index
这是性能很高的一种情况。

当查询所需的数据可以直接从索引树中检索到时，就会出现。

```
EXPLAIN
SELECT express_type
FROM t_order
ORDER BY express_type;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | t_order | index | NULL | express_type | 1 | NULL | 100187 | Using index | 


###### 4、 extra -> Using where
当有where子句时，extra都会有说明。

```
EXPLAIN
SELECT *
FROM t_order a
WHERE a.order_id > 1;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | a | range | PRIMARY | PRIMARY | 4 | NULL | 50093 | Using where | 

###### 5、 extra -> Range checked for each record
这种情况是mysql没有发现好的索引可用，速度比没有索引要快得多。

```
EXPLAIN
SELECT *
FROM t_order t, t_order_ext s
WHERE s.order_id >= t.order_id;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | s | ALL | order_id | NULL | NULL | NULL | 1 | NULL | 
| 1 | SIMPLE | t | ALL | PRIMARY | NULL | NULL | NULL | 100187 | Range checked for each record (index map: 0x1) | 


###### 6、 extra -> Using filesort
发生这种情况一般都是需要进行优化的。

MySQL 中无法利用索引完成的排序操作称为“文件排序”。

```
EXPLAIN
SELECT *
FROM t_order
ORDER BY express_type;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | t_order | ALL | NULL | NULL | NULL | NULL | 100187 | Using filesort | 



###### 7、 extra -> Using temporary
发生这种情况一般都是需要进行优化的。

mysql需要创建一张临时表用来处理此类查询。

```
EXPLAIN
SELECT *
FROM t_order a
LEFT JOIN t_order_ext b ON a.order_id=b.order_id
GROUP BY b.order_id;
```

---
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra | 
| -: | - | - | - | - | - | - | - | -: | - | 
| 1 | SIMPLE | a | ALL | NULL | NULL | NULL | NULL | 100187 | Using temporary; Using filesort | 
| 1 | SIMPLE | b | ALL | order_id | NULL | NULL | NULL | 1 | Using where; Using join buffer (Block Nested Loop) | 



