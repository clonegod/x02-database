package clonegod.jodis.sample;

import org.junit.Test;

import io.codis.jodis.JedisResourcePool;
import io.codis.jodis.RoundRobinJedisPool;
import redis.clients.jedis.Jedis;

public class JodisTest {
	
	@Test
	public void testCodisConnection() throws Exception {
		String zkAddr = "192.168.1.103:2181";
		int zkSessionTimeoutMs = 30000;
//		String zkProxyDir = "/codis3/test/proxy/";
		String zkProxyDir = "/zk/codis/db_test/proxy";
		JedisResourcePool jedisPool = RoundRobinJedisPool.create()
				.curatorClient(zkAddr, zkSessionTimeoutMs).zkProxyDir(zkProxyDir).build();
		
		try(Jedis jedis = jedisPool.getResource()) {
			String value = jedis.get("id");
			System.out.println(value);
		}
	}
	
}
