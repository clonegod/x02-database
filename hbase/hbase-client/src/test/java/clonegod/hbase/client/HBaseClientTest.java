package clonegod.hbase.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * HBase Java API 测试
 * 
 * @author clonegod
 *
 */
public class HBaseClientTest {

	public static Configuration configuration;
	public static Connection connection;
	public static Admin admin;

	@Before
	public void setUp() {
		// hbase属性配置
		configuration = HBaseConfiguration.create();
		configuration.set("hbase.zookeeper.quorum", "192.168.1.201");
		configuration.get("hbase.zookeeper.property.clientPort", "2181");
		configuration.set("zookeeper.znode.parent", "/hbase");
		configuration.set("hbase.master", "hdfs://192.168.1.201:16010");
		configuration.set("hbase.root.dir", "hdfs://192.168.1.201:8020/hbase");

		try {
			connection = ConnectionFactory.createConnection(configuration);
			admin = connection.getAdmin();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void destroy() {
		try {
			if (admin != null)
				admin.close();
			if (connection != null)
				connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final String TEST_TABLE_NAME = "mytbl01";

	/**
	 * 创建表
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateTable() throws Exception {
		TableName tableName = TableName.valueOf(TEST_TABLE_NAME);
		if (admin.tableExists(tableName)) {
			System.out.println("talbe is exists!");
		} else {
			HTableDescriptor descriptor = new HTableDescriptor(tableName); // 表名
			HColumnDescriptor columnDescriptor = new HColumnDescriptor(Bytes.toBytes("desc")); // column
																								// family
			descriptor.addFamily(columnDescriptor);
			admin.createTable(descriptor);
		}
	}

	/**
	 * 添加单条数据
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPut() throws Exception {
		Table table = connection.getTable(TableName.valueOf(TEST_TABLE_NAME));
		Put put = new Put(Bytes.toBytes("rowkey_1"));
		put.addColumn(Bytes.toBytes("desc"), Bytes.toBytes("col_1"), Bytes.toBytes("col_1:value"));
		table.put(put);

		table.close();
	}

	/**
	 * 批量添加数据
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBatchPut() throws Exception {
		Table table = connection.getTable(TableName.valueOf(TEST_TABLE_NAME));

		List<Put> putList = new ArrayList<Put>();

		for (int i = 2; i < 5; i++) {
			Put put = new Put(Bytes.toBytes("rowkey_" + i));
			put.addColumn(Bytes.toBytes("desc"), Bytes.toBytes("col_" + i), Bytes.toBytes("col_" + i + ":value"));
			putList.add(put);
		}

		table.put(putList);
		table.close();
	}

	/**
	 * 获取单条数据
	 */
	@Test
	public void testGet() throws Exception {
		Table table = connection.getTable(TableName.valueOf(TEST_TABLE_NAME));
		Get get = new Get(Bytes.toBytes("rowkey_1"));

		Result rs = table.get(get);
		byte[] byteArray = rs.getValue(Bytes.toBytes("desc"), Bytes.toBytes("col_1"));
		String columnValue = Bytes.toString(byteArray);
		assertEquals("col_1:value", columnValue);

		table.close();
	}

	/**
	 * 批量获取数据
	 */
	@Test
	public void testScan() throws Exception {
		Table table = connection.getTable(TableName.valueOf(TEST_TABLE_NAME));

		Scan scan = new Scan();
		scan.setStartRow(Bytes.toBytes("rowkey_1"));
		scan.setStopRow(Bytes.toBytes("rowkey_5"));

		ResultScanner resultScanner = table.getScanner(scan);

		for (Result result : resultScanner) {
			printCellInfo(result);
		}

		table.close();
	}

	// 打印列信息
	private void printCellInfo(Result result) {
		Cell[] cells = result.rawCells();
		for (Cell cell : cells) {
			System.out.println("rowkey:\t" + new String(CellUtil.cloneRow(cell)));
			System.out.println("Timetamp:\t" + cell.getTimestamp());
			System.out.println("column Family:\t" + new String(CellUtil.cloneFamily(cell)));
			System.out.println("column Name:\t" + new String(CellUtil.cloneQualifier(cell)));
			System.out.println("column Value:\t" + new String(CellUtil.cloneValue(cell)));
			System.out.println("=========================================================");
		}

	}

	/**
	 * 删除数据
	 */
	@Test
	public void testDelete() throws Exception {
		Table table = connection.getTable(TableName.valueOf(TEST_TABLE_NAME));

		Delete delete = new Delete(Bytes.toBytes("rowkey_3"));

		table.delete(delete);

		table.close();
	}

	/**
	 * 删除表
	 */
	@Test
	public void testDeleteTable() throws Exception {
		TableName tlbName = TableName.valueOf(TEST_TABLE_NAME);
		if (!admin.tableExists(tlbName)) {
			return;
		}
		admin.disableTable(tlbName);
		admin.deleteTable(tlbName);
	}

}
