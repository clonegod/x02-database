package clonegod.hbase.client;

import java.io.IOException;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;

/**
 * Hbase表定义相关操作
 * 	- 创建、删除表
 * 	- 创建、删除列簇
 *
 */
// TODO 建表时设置预分区选项，列簇的属性定制
public class HBaseOpsForAdmin {
	
	/**
	 * 列出Hbase数据库所有的表
	 */
	public static void listTables() throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Admin admin = connection.getAdmin()) {
			HTableDescriptor[] descriptors = admin.listTables();
			System.out.println("============================ALL TABLE===========================");
			for(HTableDescriptor desc : descriptors) {
				System.out.println(desc.toStringCustomizedValues());
			}
		}
	}

	public static void createTable(String tblName, String columnFamily, boolean dropIfExists) throws IOException {
		createTable(tblName, columnFamily, 1, dropIfExists);
	}
	
	/**
	 * 创建表
	 * @param tblName
	 * @param columnFamily
	 * @param maxVersion
	 * @param dropIfExists
	 * @throws IOException
	 */
	public static void createTable(String tblName, String columnFamily, int maxVersion, boolean dropIfExists) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Admin admin = connection.getAdmin()) {
			
			HTableDescriptor table = new HTableDescriptor(TableName.valueOf(tblName));

			System.out.print("Creating table: " + tblName);
			if (admin.tableExists(table.getTableName())) {
				if(! dropIfExists) {
					return;
				}
				admin.disableTable(table.getTableName());
				admin.deleteTable(table.getTableName());
			}
			HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(columnFamily)
					.setCompressionType(Algorithm.NONE)
					.setMaxVersions(Math.min(3, Math.max(1, maxVersion)));
			table.addFamily(hColumnDescriptor);
			admin.createTable(table);
			System.out.println("Create Table Done.");
		}
	}
	
	/**
	 * 删除表
	 * @param tblName
	 * @throws IOException
	 */
	public static void deleteTable(String tblName) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Admin admin = connection.getAdmin()) {
			TableName tableName = TableName.valueOf(tblName);

			checkBeforeTableOps(tableName, admin);

			// Disable an existing table
			admin.disableTable(tableName);

			// Delete a table (Need to be disabled first)
			admin.deleteTable(tableName);
		}
	}

	/**
	 * 增加列簇
	 * @param tblName
	 * @param columnFamily
	 * @throws IOException
	 */
	public static void addColumnFamily(String tblName, String columnFamily) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Admin admin = connection.getAdmin()) {

			TableName tableName = TableName.valueOf(tblName);

			checkBeforeTableOps(tableName, admin);

			// Update existing table
			HColumnDescriptor newColumn = new HColumnDescriptor(columnFamily);
			newColumn.setCompactionCompressionType(Algorithm.GZ);
			newColumn.setMaxVersions(HConstants.ALL_VERSIONS);
			admin.addColumn(tableName, newColumn);
		}
	}

	/**
	 * 删除列簇
	 * @param tblName
	 * @param columnFamily
	 * @throws IOException
	 */
	public static void deleteColumnFamily(String tblName, String columnFamily) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Admin admin = connection.getAdmin()) {

			TableName tableName = TableName.valueOf(tblName);

			checkBeforeTableOps(tableName, admin);

			// Delete an existing column family
			admin.deleteColumn(tableName, columnFamily.getBytes("UTF-8"));
			
		}
	}
	
	
	private static void checkBeforeTableOps(TableName tblName, Admin admin) throws IOException {

		if (!admin.tableExists(tblName)) {
			System.out.println("Table does not exist.");
			throw new RuntimeException("Table does not exist.");
		}
		
		// more check ...
		
	}

}
