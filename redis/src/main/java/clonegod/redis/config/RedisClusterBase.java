package clonegod.redis.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClusterBase {
	protected JedisCluster jedisCluster;
	
	// 集群节点
	private static final List<String> CLUSTER_NODES = Arrays.asList(
								"192.168.1.201:7001", "192.168.1.201:7004",
								"192.168.1.201:7002", "192.168.1.201:7005",
								"192.168.1.201:7003", "192.168.1.201:7006"
							); 
	private int connectionTimeout = 10000;
	private int soTimeout = 10000;
	private int maxAttempts = 3; 
	private String password = null;//"changeit";
	
	private Set<HostAndPort> nodes = new HashSet<>();
	
	public RedisClusterBase() {
        GenericObjectPoolConfig poolConfig = buildPoolConfig(500, 200, 100, 10000);
        for (String ipPort : CLUSTER_NODES) {
        	String[] ipPortPair = ipPort.split(":");
        	nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        jedisCluster = new JedisCluster(nodes, 
        								connectionTimeout, 
        								soTimeout, 
        								maxAttempts, 
        								password, 
        								poolConfig);
	}
	
	private JedisPoolConfig buildPoolConfig(int maxTotal, int maxIdle, int minIdle, int maxWaitMillis) {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWaitMillis);
		poolConfig.setTestOnBorrow(true);
		return poolConfig;
	}
	
	/**
	 * 集群使用过程中不可以调用jedisCluster.close();  
	 * 否则客户端会断开与cluster的连接，导致连接池的所有连接都不可用。
	 */
	protected void close() throws IOException {
		jedisCluster.close();
	}
}
