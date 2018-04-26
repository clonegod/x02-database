package clonegod.redis.cluster;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clonegod.redis.config.RedisClusterBase;
import redis.clients.jedis.SortingParams;

public class Test03List extends RedisClusterBase {
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
	 * 列表-队列，先进先出
	 */
	@Test
	public void testListQueue() {
		String key = "LIST-QUEUE";
		jedisCluster.del(key);
		
		System.out.println(jedisCluster.lpush(key, "1", "2"));
		System.out.println(jedisCluster.lpush(key, "3", "4", "5"));
		System.out.println(jedisCluster.lpush(key, "6"));
		
		System.out.println("lrange:" + jedisCluster.lrange(key, 0, -1));
		System.out.println("index list[2]: " + jedisCluster.lindex(key, 2));

		// 先进先出
		for(int i = 0; i < 6; i++) {
			System.out.print(jedisCluster.rpop(key) + "\t");
		}
	}
	
	/**
	 * 列表-排序
	 */
	@Test
	public void testListSort() {
		String key = "LIST-QUEUE";
		jedisCluster.del(key);
		
		System.out.println(jedisCluster.lpush(key, "1", "2", "200", "4", "9"));
		System.out.println(jedisCluster.lrange(key, 0, -1));
		
		// 排序，不影响原始集合的元素顺序
		System.out.println("sort:" + jedisCluster.sort(key, new SortingParams().asc()));
		System.out.println("sort:" + jedisCluster.sort(key, new SortingParams().desc()));
		
		System.out.println(jedisCluster.lrange(key, 0, -1));
		
	}
	
	/**
	 * 列表-定长列表，先进先出
	 * 
	 */
	@Test
	public void testFixLenQueue() throws InterruptedException {
		String key = "LIST-QUEUE";
		jedisCluster.del(key);
		
		final int fixLen = 5;
		
		System.out.println(jedisCluster.lpush(key, "1", "2", "3", "4", "5", "6", "7", "8"));
		
		System.out.println("llen:" + jedisCluster.llen(key)); // 获取集合元素个数
		System.out.println("lrange:" + jedisCluster.lrange(key, 0, -1)); // 打印集合元素
		
		/**
         * 该命令将仅保留指定范围内的元素
         * 每次lpush以后，就用ltrim进行截取，已达到定长队列（或定长list）的目的
         *
         * 注意：
         *    超出固定长度，即表示预先设置的阈值已经被突破，此时应该有相应机制来处理。
         */
		for(int i = 9; i < 12; i++) {
			System.out.println(jedisCluster.lpush(key, ""+i)); // 每次push新元素后，就重新截取列表
			System.out.println(jedisCluster.ltrim(key, 0, fixLen)); // ltrim，保留最新的N个元素 
			System.out.println(jedisCluster.lrange(key, 0, -1));
			Thread.sleep(1000);
		}
		
	}
}
