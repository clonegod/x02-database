# Explain 用法

explain 可以帮助我们分析 select 语句，让我们知道查询效率低下的原因，从而改进我们查询，
让查询优化器能够更好的工作。


EXPLAIN 的每个输出行代表查询中某一个表的相关信息，并且每个行包括下面的列：

## id 列
列 | 说明
------------ | -------------
id | MySQL Query Optimizer 选定的执行计划中查询的序列号。表示查询中执行 select 子句或操作表的顺序，id 值越大优先级越高，越先被执行。id 相同，执行顺序由上至下。


## select_type 列
项 | 说明
------------ | -------------
SIMPLE | 简单查询 (不使用UNION或子查询)
PRIMARY | 外层查询，主查询
UNION | UNION 中的第二个或随后的 select 查询，不依赖于外部查询的结果集。
DEPENDENT UNION | UNION 中的第二个或随后的 select 查询，依赖于外部查询的结果集。
UNION RESULT | UNION 查询的结果集。
SUBQUERY | 子查询中的第一个 select 查询，不依赖于外部查询的结果集。
DEPENDENT SUBQUERY | 子查询中的第一个 select 查询，依赖于外部查询的结果集。
DERIVED |  用于 from 子句里有子查询的情况。MySQL会递归执行这些子查询，把结果放在临时表里。
UNCACHEABLE SUBQUERY | 结果集不能被缓存的子查询，必须重新为外层查询的每一行进行评估。
UNCACHEABLE UNION | UNION 中的第二个或随后的 select 查询，属于不可缓存的子查询。


## table 列
列 | 说明
------------ | -------------
table | 输出行所引用的表。


## type 列
显示表连接使用的类型，表格按最优到最差的类型排序。

项| 说明
------------ | -------------
system | 表仅有一行(=系统表)。这是 const 连接类型的一个特例。
const  |  表最多有一个匹配行,它将在查询开始时被读取。因为仅有一行,在这行的列值可被优化器剩余部分认为是常数。const表很快,因为它们只读取一次!
eq_ref  | 对于每个来自于前面的表的行组合,从该表中读取一行。这可能是最好的联接类型,除了const类型。
ref  |  对于每个来自于前面的表的行组合,所有有匹配索引值的行将从这张表中读取。
ref_or_null  |  该联接类型如同ref,但是添加了MySQL可以专门搜索包含NULL值的行。
index_merge  |  该联接类型表示使用了索引合并优化方法。
unique_subquery   |  该类型替换了下面形式的IN子查询的ref: value IN (SELECT primary_key FROM single_table WHERE some_expr) unique_subquery是一个索引查找函数,可以完全替换子查询,效率更高。
index_subquery  | 该联接类型类似于unique_subquery。可以替换IN子查询,但只适合下列形式的子查询中的非唯一索引: value IN (SELECT key_column FROM single_table WHERE some_expr)
range  |  只检索给定范围的行，使用一个索引来选择行。当使用=、<>、>、>=、<、<=、IS NULL、<=>、BETWEEN或者 IN 操作符，用常量比较关键字列时，就可能使用range。
index  | 该联接类型与ALL相同,除了只有索引树被扫描。这通常比ALL快,因为索引文件通常比数据文件小。
all  | 最坏的情况，从头到尾全表扫描。对于每个来自于先前的表的行组合,进行完整的表扫描。



## possible_keys 列
列 | 说明
------------ | -------------
possible_keys  | 指出MySQL可能使用该表中的哪些索引。如果为空，说明没有可用的索引。


## key 列
列 | 说明
------------ | -------------
key  | 显示MySQL实际决定使用的键(索引)。如果没有选择索引,键是NULL。MySQL 实际从 possible_key 选择使用的索引。如果为 NULL，则没有使用索引。很少的情况下，MYSQL 会选择优化不足的索引。这种情况下，可以在 SELECT 语句中使用 USE INDEX（indexname）来强制使用一个索引或者用IGNORE INDEX（indexname）来强制 MYSQL忽略索引。



## key_len 列
列 | 说明
------------ | -------------
key_len  | 显示MySQL决定使用的键长度。如果键是NULL,则长度为NULL。在不损失精确性的情况下，长度越短越好。



## ref 列
列 | 说明
------------ | -------------
ref  | 显示使用哪个列或常数，与key一起从表中选择行。


## rows 列
列 | 说明
------------ | -------------
rows  | 显示MySQL认为它执行查询时必须检查的行数。多行之间的数据相乘可以估算要处理的行数。



## filtered 列
列 | 说明
------------ | -------------
filtered  | 指返回结果的行占需要读到的行(rows列的值)的百分比。 此值好像一直都是100，为什么？



## Extra 列
该列包含MySQL解决查询的详细信息。

列 | 说明
------------ | -------------
Distinct  | 一旦MYSQL找到了与行相联合匹配的行，就不再搜索了。
Not exists   | MYSQL优化了LEFT JOIN，一旦它找到了匹配LEFT JOIN标准的行，就不再搜索了。
Using where | 使用了WHERE从句来限制哪些行将与下一张表匹配或者是返回给用户。
Using index   | 列数据是从仅仅使用了索引中的信息而没有读取实际的行动的表返回的，这发生在对表的全部的请求列都是同一个索引的部分的时候。 
Range checked for each   | 没有找到理想的索引，因此对于从前面表中来的每一个行组合，MYSQL检查使用哪个索引，并用它来从表中返回行。这是使用索引的最慢的连接之一 。
Using filesort   | 看到这个的时候，查询就需要优化了。表示 MySQL 会对结果使用一个外部索引排序，而不是从表里按索引次序读到相关内容。可能在内存或者磁盘上进行排序。MySQL 中无法利用索引完成的排序操作称为“文件排序”。
Using temporary | 看到这个的时候，查询需要优化了。为了解决查询,MySQL需要创建一个临时表来容纳结果。
