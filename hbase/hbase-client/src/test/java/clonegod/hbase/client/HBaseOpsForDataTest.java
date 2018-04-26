package clonegod.hbase.client;

import static clonegod.hbase.client.HbaseOpsForData.put;
import static clonegod.hbase.client.HbaseOpsForData.toPut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
案例；
用HBase存储社交网站站内短信信息，要求记录发送者，接受者，时间，内容，有关的查询是:
	发送者可以列出他所有（或按时间段）发出的信息列表（按时间降序排列），
	接收者可以列出他所有（或按时间段）收到的信息列表（按时间降序排列）。请进行数据建模。

设计rowkey
rowkey结构：md5(sender)|datetime

family:
	col1: sender
	col2: receiver
	col3: content
-------------------------------------------------------
下面的rowkey设计可以从发送者或接受者维度利用rowkey进行查询，但是会导致数据量增加1倍，不是很好。
需要支持3个维度的查询：用户、时间、消息方向。因此，采用复合索引来实现。
每条消息需要维护发送者、接收者的相关数据：
<uid1>-<type>-<datetime> : <colfam> : <uid2> : <content>
 
<发送者>-<发送0>-<时间> : <cf-列簇> : <col-接收者> : <内容>
<接收者>-<接收1>-<时间> : <cf-列簇> : <col-发送者> : <内容>

案例设计
u1发送1个消息给u2
u1发送1个消息给u3
u2发送1个消息给u3
u3发送2个消息给u1
 */
public class HBaseOpsForDataTest {
	
	String tableName = "sms";
	
	String family_main = "cf-main";
	String col_sender = "sender";
	String col_receiver = "receiver";
	String col_content = "content";
	
	String family_ext = "cf-ext";
	String col_success = "success";
	
	
	//////////////////////////////////////////////////////////////////////////
	
	@Test
	public void test_put() throws IOException {
		MSG msg = new MSG();
		put(tableName, toPut(msg.rowkey, family_main, col_sender, msg.sender));
		put(tableName, toPut(msg.rowkey, family_main, col_receiver, msg.receiver));
		put(tableName, toPut(msg.rowkey, family_main, col_content, msg.content));
	}
	
	@Test
	public void test_putBatch() throws Exception {
		List<Put> puts = new ArrayList<>();
		for(int i = 0; i < 20; i++) {
			MSG msg = new MSG();
			puts.add(toPut(msg.rowkey, family_main, col_sender, msg.sender));
			puts.add(toPut(msg.rowkey, family_main, col_receiver, msg.receiver));
			puts.add(toPut(msg.rowkey, family_main, col_content, msg.content));
			Thread.sleep(500+new Random().nextInt(2000));
		}
		HbaseOpsForData.batchPutAsync(tableName, puts);
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	
	
	@Test
	public void test_delete() throws IOException {
		List<Result> results = HbaseOpsForData.scan(tableName, family_main, null);
		if(CollectionUtils.isNotEmpty(results)) {
			Result r = results.get(0);
			String rowkey = new String(r.getRow());
			System.out.println("-------delete row: " + rowkey);
			HbaseOpsForData.delete(tableName, rowkey);
		}
	}
	
	@Test
	public void test_deleteBatch() throws IOException {
		List<Result> results = HbaseOpsForData.scan(tableName, family_main, null);
		String rowkey = null;
		List<Delete> rows = new ArrayList<>();
		for(Result r : results) {
			rowkey = new String(r.getRow());
			rows.add(HbaseOpsForData.toDelete(rowkey));
			System.out.println("-------delete row: " + rowkey);
		}
		HbaseOpsForData.batchDeleteAsync(tableName, rows);
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	
	@Test
	public void test_get() throws IOException {
		Result r = HbaseOpsForData.get(tableName, "alice|bob|20171008041938664");
		HbaseOpsForData.format(r);
	}
	
	@Test
	public void test_scanByRowkey() throws IOException {
		List<Result> results = null;
		results = HbaseOpsForData.scan(tableName, family_main, null, "alice|0", "alice|z");
		HbaseOpsForData.format(results);
		
	}
	
	/**
	 * 基于列值的过滤
	 * SingleColumnValueFilter
	 */
	@Test
	public void test_scanByFilter() throws IOException {
		List<Result> results = null;
		
		// 查询所有发送给alice的消息
        SingleColumnValueFilter filter = new SingleColumnValueFilter(
                Bytes.toBytes(family_main),
                Bytes.toBytes(col_receiver),
                CompareOp.EQUAL,
                Bytes.toBytes("alice")
                );
        
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        filterList.addFilter(filter);
		
        results = HbaseOpsForData.scan(tableName, family_main, null, filterList);
		HbaseOpsForData.format(results);
		
	}

	/**
	 * 基于列值的过滤
	 * SingleColumnValueFilter.RegexStringComparator: 正则匹配
	 */
	@Test
	public void test_scanByFilterOfRegexStringComparator() throws IOException {
		List<Result> results = null;
		
		// 查询消息内容中包含2017100821481的row
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(family_main),
				Bytes.toBytes(col_content),
				CompareOp.EQUAL,
				new RegexStringComparator("2017100821481") // <--- 指定正则表达式
				);
		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
		filterList.addFilter(filter);
		
		results = HbaseOpsForData.scan(tableName, family_main, null, filterList);
		HbaseOpsForData.format(results);
		
	}
	
	/**
	 * 基于列值的过滤
	 * SingleColumnValueFilter.SubstringComparator: 字符串包含（大小写不敏感）
	 */
	@Test
	public void test_scanByFilterOfSubstringComparator() throws IOException {
		List<Result> results = null;
		
		// 查询所有bob发送给alice的消息
		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				Bytes.toBytes(family_main),
				Bytes.toBytes(col_content),
				CompareOp.EQUAL,
				new SubstringComparator("bob send msg to alice") // <--- 指定子串
				);
		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
		filterList.addFilter(filter);
		
		results = HbaseOpsForData.scan(tableName, family_main, null, filterList);
		HbaseOpsForData.format(results);
		
	}
	
	/**
	 * 基于RowKey的过滤 --- 匹配rowkey中某个偏移量上的字符串，而不是按rowkey前缀匹配。
	 * 
	 * 注意：当需要根据行键特征查找一个范围的行数据时，使用Scan的startRow和stopRow会更高效，但是，startRow和stopRow只能匹配行键的开始字符，而不能匹配中间包含的字符：
        byte[] startColumn = Bytes.toBytes("aaa");
        byte[] endColumn = Bytes.toBytes("bbb");
        Scan scan = new Scan(startColumn,endColumn);
        
	 * 当需要针对行键进行更复杂的过滤时，可以使用RowFilter。
	 */
	@Test
	public void test_scanByFilterOfRowFilter() throws IOException {
		List<Result> results = null;
		
		// 查询所有发送给alice的消息。可以指定多个匹配器
		RowFilter filter1 = new RowFilter(
                CompareOp.EQUAL , 
                new SubstringComparator("|alice|") // 字符串包含
                );
		RowFilter filter2 = new RowFilter(
				CompareOp.EQUAL , 
				new RegexStringComparator("^.{6}alice", Pattern.DOTALL)   // 正则匹配
				);
		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE); // 满足一个过滤器即可
		filterList.addFilter(filter1);
		filterList.addFilter(filter2);
		
		results = HbaseOpsForData.scan(tableName, family_main, null, filterList);
		HbaseOpsForData.format(results);
		
	}
	
	
	/**
	 * 分页查询
	 * PageFilter  设置pageSize
	 * 注意：由于该过滤器并不能保证返回的结果行数小于等于指定的页面行数，所以更好的返回指定行数的办法是ResultScanner.next(int nbRows) 
	 */
	@Test
	public void test_scanByFilterOfPageFilter () throws IOException {

		List<Result> results = new ArrayList<>();
		
		String startRow = "0";
		int pageSize = 8;
		int firstSize = Math.max(1, new Random().nextInt(pageSize));
		
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Table table = connection.getTable(TableName.valueOf(tableName))) {
			
			PageFilter pageFilter = new PageFilter(pageSize); // 设置页面大小---scanner最多扫描并返回的行
			Scan scan = new Scan(Bytes.toBytes(startRow), pageFilter);
			
			ResultScanner resultScanner = table.getScanner(scan);
			
			// 第1次scan仅获取n条
			for (Result result : resultScanner.next(firstSize)) {
				results.add(result);
			}
			HbaseOpsForData.format(results);
			
			
			// get next page 
			System.out.println("------next page:");
			results.clear();
			// 第2次scan获取所有剩余的行
			for (Result result : resultScanner) {
				results.add(result);
			}
			HbaseOpsForData.format(results);
			
			
			resultScanner.close();
		}
	
	}
	
	/**
	 * 分页查询
	 * ResultScanner.next(n) 设置pageSize
	 */
	@Test
	public void test_scanOfResultScannerNext() throws IOException {
		List<Result> results = new ArrayList<>();
		
		int pageSize = 3;
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Table table = connection.getTable(TableName.valueOf(tableName))) {
			Scan scan = new Scan();
			
			ResultScanner resultScanner = table.getScanner(scan);
			
			for (Result result : resultScanner.next(pageSize)) {
				results.add(result);
			}
			HbaseOpsForData.format(results);
			
			
			// get next page 
			System.out.println("------next page:");
			results.clear();
			for (Result result : resultScanner.next(pageSize)) {
				results.add(result);
			}
			HbaseOpsForData.format(results);
			
			
			resultScanner.close();
		}
	}
	
}
