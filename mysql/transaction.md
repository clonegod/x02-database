#### mysql中set autocommit=0与start transaction区别

```
By default, MySQL runs with autocommit mode enabled. 
This means that as soon as you execute a statement that updates (modifies) a table,
MySQL stores the update on disk to make it permanent. 
The change cannot be rolled back.

To disable autocommit mode implicitly for a single series of statements,
use the START TRANSACTION statement:

START TRANSACTION;
SELECT @A:=SUM(salary) FROM table1 WHERE type=1;
UPDATE table2 SET summary=@A WHERE type=1;
COMMIT;

-- ROLLBACK；

With START TRANSACTION, autocommit remains disabled until you end the transaction with COMMIT or ROLLBACK. 
The autocommit mode then reverts to its previous state.

```

## 全局设置
```
set @@autocommit:=0;
select @@autocommit;
```

##### 查看autocommit当前设置的值
```
SHOW VARIABLES LIKE 'autocommit';
```

---
| Variable_name | Value | 
| - | - | 
| autocommit | OFF | 
