package clonegod.redis.cluster;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clonegod.redis.config.RedisClusterBase;

public class Test02Hash extends RedisClusterBase {
	static String TEST_KEY_PREFIX = "MY_KEY_PREFIX:";
	static String TEST_KEY = TEST_KEY_PREFIX + "Alice";
	
	@Before
	public void setUp() {
        // 单元测试执行前先删除历史key
        jedisCluster.del(TEST_KEY);
	}
	
	@After
	public void tearDown() throws IOException {
		jedisCluster.close();
	}
	
	
	/**
	 * hash-数据结构
	 * 		key1 : field1 -> value1	
	 * 		key1 : field2 -> value2
	 * 	
	 * 		key2 : field3 -> value3	
	 * 		key2 : field4 -> value4	
	 * 		key2 : field5 -> value5	
	 */
	@Test
	public void testHashOpers() throws InterruptedException {
		String hashKey = "BLACKLIST";
		jedisCluster.del(hashKey);
		
		System.out.println(jedisCluster.hset(hashKey, "name", "zs"));
		System.out.println(jedisCluster.hexists(hashKey, "name"));
		
		System.out.println(jedisCluster.hset(hashKey, "age", "18"));
		System.out.println(jedisCluster.hset(hashKey, "score", "80.50"));
		
		System.out.println("hkeys:" + jedisCluster.hkeys(hashKey));
		System.out.println("hvals:" + jedisCluster.hvals(hashKey));
		System.out.println("hgetAll:" + jedisCluster.hgetAll(hashKey));
		
		System.out.println(jedisCluster.hincrBy(hashKey, "age", 2));
		System.out.println(jedisCluster.hincrByFloat(hashKey, "score", 0.5));
		System.out.println("hmget" + jedisCluster.hmget(hashKey, "age", "score"));
		
		System.out.println("hdel" + jedisCluster.hdel(hashKey, "score"));
		System.out.println(jedisCluster.hgetAll(hashKey));
		
		System.out.println("hsetnx:" + jedisCluster.hsetnx(hashKey, "id", "1"));
		System.out.println("hsetnx:" + jedisCluster.hsetnx(hashKey, "id", "2"));
		
		System.out.println("expire:" + jedisCluster.expire(hashKey, 3));
		
		for(int i = 0; i < 5; i++) {
			System.out.println(jedisCluster.hgetAll(hashKey));
			Thread.sleep(1000);
		}
	}
}
