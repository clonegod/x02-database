package clonegod.hbase.client;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * 全局共享的Configuration，用于创建Connection用。
 *
 */
public abstract class HbaseConfig {
	
	public static Configuration CONFIG = HBaseConfiguration.create();

	static {
		CONFIG.set("hbase.zookeeper.quorum", "192.168.1.201");
		CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		CONFIG.set("zookeeper.znode.parent", "/hbase");
		CONFIG.set("hbase.master", "hdfs://192.168.1.201:16000");	// HMaster的监听地址
		CONFIG.set("hbase.root.dir", "hdfs://192.168.1.201:8020/hbase"); // hbase在HDFS上存储数据的工作目录
		CONFIG.set("hbase.rpc.timeout", "2000"); 
		CONFIG.set("hbase.client.operation.timeout", "3000"); 
		CONFIG.set("hbase.client.scanner.timeout.period", "10000"); 
	}

}
