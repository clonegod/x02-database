package clonegod.hbase.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Hbase数据操作
 * 	put, get, scan, delete 
 * 
 */
public class HbaseOpsForData {
	
	private static final int WRITE_BUFFER_SIZE = 5 * 1024 * 1024;

	
	/****************************************************************
	 * 								插入/更新
	 * **************************************************************/
	
	/**
	 * 构造Put对象
	 */
	public static Put toPut(String rowkey, String cf, String col, String data) {
		Put put = new Put(Bytes.toBytes(rowkey));
		put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(col), Bytes.toBytes(data));
		return put;
	}
	
	/*
	 *  1. Hbase不区分插入和更新操作
	 *     Put命令可以执行插入一行新数据和更新现有数据
	 *    	- 当执行Put命令时，如果rowkey不存在，将会插入一行新数据
	 *    	- 当执行Put命令时，如果rowkey存在，将执行更新操作(如果是配置了多版本，则插入数据的新版本)
	 *  2. 更新操作只在某个指定的column上发生，不会影响到其它的column
	 *  3. 同一个 table 对象,可以调用多次put方法,添加put对象 --- 使用批量插入数据
	 */
	/**
	 * 单条插入
	 * @param tbl
	 * @param put
	 * @throws IOException
	 */
	public static void put(String tbl, Put put) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Table table = connection.getTable(TableName.valueOf(tbl))) {
			table.put(put);
		}
	}
	
	/**
	 * 单条插入-异步
	 * 
	 * @param tbl
	 * @param put
	 * @throws IOException
	 */
	public static void putAsync(String tbl, Put put) throws IOException {
		List<Put> single = new ArrayList<Put>();
		single.add(put);
		batchPutAsync(tbl, single);
	}
	
	/**
	 * 批量插入数据-异步
	 * 
	 * @param tbl
	 * @param puts
	 * @throws IOException
	 */
	public static void batchPutAsync(String tbl, List<Put> puts) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG)) {
	        BufferedMutatorParams params = 
	        		new BufferedMutatorParams(TableName.valueOf(tbl))
	        			.listener(new AsyncOpsExceptionListener())
						.writeBufferSize(WRITE_BUFFER_SIZE);
	        BufferedMutator mutator = connection.getBufferedMutator(params);  
	        try {
	            mutator.mutate(puts);  
	            mutator.flush();
	        } finally {
	            mutator.close();  
	        }  
		}
	}
	
	/**
	 * 异步批量插入，删除数据的公共方法
	 */
	private static void batchAsync(String tbl, List<? extends Mutation> list) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG)) {
			BufferedMutatorParams params = 
					new BufferedMutatorParams(TableName.valueOf(tbl))
					.listener(new AsyncOpsExceptionListener())
					.writeBufferSize(WRITE_BUFFER_SIZE);
			try (BufferedMutator mutator = connection.getBufferedMutator(params);) {
				mutator.mutate(list);
				mutator.flush();
			}
		}
	}
	
	/****************************************************************
	 * 								删除
	 * **************************************************************/
	
	/**
	 * 构造Delete对象
	 */
	public static Delete toDelete(String rowkey) {
		Delete d = new Delete(rowkey.getBytes());
		return d;
	}
	
	/**
	 * 删除rowkey关联的所有数据
	 * 注意：会删除关联的所有记录(all versions of all columns in all families)
	 * delete everything associated with the specified row (all versions of all columns in all families).
	 * 
	 * @param tbl
	 * @param rowkey
	 * @throws IOException
	 */
	public static void delete(String tbl, String rowkey) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Table table = connection.getTable(TableName.valueOf(tbl))) {
			Delete d = new Delete(rowkey.getBytes());  
			table.delete(d);
		}
	}
	
	/**
	 * 仅删除rowkey关联的某个列
	 * 
	 * @param tbl
	 * @param rowkey
	 * @param columnFamily
	 * @param column
	 * @throws IOException
	 */
	public static void deleteColumnOnly(String tbl, String rowkey, String family, String qualifier) throws IOException {
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Table table = connection.getTable(TableName.valueOf(tbl))) {
			Delete delete = toDelete(rowkey);
			delete.addColumns(family.getBytes(), qualifier.getBytes());
			table.delete(delete);
		}
	}
	
	/**
	 * 删除rowkey-异步
	 * @param tbl
	 * @param delete
	 * @throws IOException
	 */
	public static void deleteAsync(String tbl, Delete delete) throws IOException {
		List<Delete> single = new ArrayList<>();
		single.add(delete);
		batchDeleteAsync(tbl, single);
	}
	
	
	/**
	 * 删除rowkey--批量，异步
	 * 注意：会删除关联的所有记录(all versions of all columns in all families)
	 * 
	 * @param tbl
	 * @param rowkeys
	 * @throws IOException
	 */
	public static void batchDeleteAsync(String tbl, List<Delete> rows) throws IOException {
		batchAsync(tbl, rows);
	}
	
	
	/****************************************************************
	 * 								查询
	 * @return 
	 * **************************************************************/

	/**
	 * 根据rowkey查询
	 * 
	 * @param tbl
	 * @param rowkey
	 * @return
	 * @throws IOException
	 */
	public static Result get(String tbl, String rowkey) throws IOException {
		Result result = null;
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Table table = connection.getTable(TableName.valueOf(tbl))) {
			Get get = new Get(Bytes.toBytes(rowkey));
			result = table.get(get);
		}
		return result;
	}
	
	
	/*
	 * 批量获取多条数据（根据rowkey进行范围查找）
	 * 	startRowKey 起始rowkey
	 *  stopRowKey	结束rowkey，结果集不包含stopRowKey
	 * 
	 * Scan结果可以批量从缓存取得，提高执行效率
	 *  – 执行效率提升，但内存使用将会增加
	 */
	
	/**
	 * 扫描查询 - 
	 * 
	 * @param tbl	表名
	 * @param cf	列簇
	 * @param cols	字段
	 * @return
	 * @throws IOException
	 */
	public static List<Result> scan(String tbl, String cf, String[] cols) throws IOException {
		return scan(tbl, cf, cols, null, null, null, null, null);
	}
	
	/**
	 * 扫描查询 - 使用rowkey进行范围限定
	 * 
	 * @param tbl	表名
	 * @param cf	列簇
	 * @param cols	字段
	 * @param startRow	起始rowkey
	 * @param stopRow	结束rowkey（不包含）
	 * @return
	 * @throws IOException
	 */
	public static List<Result> scan(String tbl, String cf, String[] cols, 
			String startRow, String stopRow) throws IOException {
		return scan(tbl, cf, cols, startRow, stopRow, null, null, null);
	}
	
	/**
	 * 扫描查询 - 使用filter过滤
	 * 
	 * @param tbl
	 * @param cf
	 * @param cols
	 * @param filters
	 * @return
	 * @throws IOException
	 */
	public static List<Result> scan(String tbl, String cf, String[] cols, 
			FilterList filters) throws IOException {
		return scan(tbl, cf, cols, null, null, null, null, filters);
	}
	
	/**
	 * 扫描查询
	 * 	限定列簇、列（只查询需要数据）
	 * 	基于rowkey检索速度最快
	 * 	可限定在某个时间范围内进行查询
	 * 
	 * @param tbl		表名称
	 * @param cf		列簇名称
	 * @param cols		单列或多列
	 * @param startRow	起始rowkey
	 * @param stopRow	结束rowkey（不包含）
	 * @param minStamp	起始时间
	 * @param maxStamp	结束时间
	 * @return
	 * @throws IOException
	 */
	public static List<Result> scan(String tbl, String cf, String[] cols, 
			String startRow, String stopRow,
			Long minStamp, Long maxStamp, 
			FilterList filters) throws IOException {
		
		List<Result> results = new ArrayList<>();
		
		try (Connection connection = ConnectionFactory.createConnection(HbaseConfig.CONFIG);
				Table table = connection.getTable(TableName.valueOf(tbl))) {
			Scan scan = new Scan();
			
			// 缩小查询范围
			if(cols == null || cols.length == 0) {
				scan.addFamily(Bytes.toBytes(cf));
			} else {
				for(String col : cols) {
					scan.addColumn(Bytes.toBytes(cf), Bytes.toBytes(col));
				}
			}
			
			if(startRow != null) {
				scan.setStartRow(Bytes.toBytes(startRow));
			}
			if(stopRow != null) {
				scan.setStopRow(Bytes.toBytes(stopRow));
			}
			
			if(minStamp != null && maxStamp != null) {
				scan.setTimeRange(minStamp, maxStamp);
			}
			
			scan.setMaxVersions(1); // 返回最新数据
			
			// see HConstants.HBASE_CLIENT_SCANNER_CACHING = "hbase.client.scanner.caching"
			scan.setCaching(1000); // 缓存1000条数据---blockCache
			
			if(filters == null) {
				// Cannot set batch on a scan using a filter that returns true for filter.hasFilterRow
				scan.setBatch(1000); // 限定一次最多返回多少个key-value
			} else {
				scan.setFilter(filters);
			}
			ResultScanner resultScanner = table.getScanner(scan);

			for (Result result : resultScanner) {
				results.add(result);
			}
			
			resultScanner.close();
		}
		
		return results;
	}
	
	
	/**
	 * 格式化输出结果 
	 */
	public static void format(List<Result> results) {
		for(Result r : results) {
			format(r);
		}
	}
	
	/**
	 * 格式化输出结果 
	 */
	public static void format(Result result) {
		StringBuilder buf = new StringBuilder();
		for (Cell cell : result.rawCells()) {
			buf.append("rowkey: " + new String(CellUtil.cloneRow(cell))).append("\t");
			buf.append("Timetamp: " + cell.getTimestamp()).append("\n");
			buf.append("column Family: " + new String(CellUtil.cloneFamily(cell))).append("\t");
			buf.append("column Qulifier: " + new String(CellUtil.cloneQualifier(cell))).append("\t");
			buf.append("column Value: " + new String(CellUtil.cloneValue(cell))).append("\n");
		}
		System.out.println("=========================================================");
		System.out.println(buf.toString());
	}
	
}

