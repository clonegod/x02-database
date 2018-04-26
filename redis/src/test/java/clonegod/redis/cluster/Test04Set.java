package clonegod.redis.cluster;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clonegod.redis.config.RedisClusterBase;

public class Test04Set extends RedisClusterBase {
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
	 * set - 无序不重复的集合
	 */
	@Test
	public void testSet() {
		/**
		 * redis.clients.jedis.exceptions.JedisDataException: WRONGTYPE Operation against a key holding the wrong kind of value
		 * jedisCluster.sadd("key1", "value1", "value2");
		 */
		
		String key1 = String.format("{%s}:%s", TEST_KEY_PREFIX, "k1");
		String key2 = String.format("{%s}:%s", TEST_KEY_PREFIX, "k2");
		
		jedisCluster.del(key1, key2);
		
		//给集合添加数据
		jedisCluster.sadd(key1, "1", "2", "3"); 
		jedisCluster.sadd(key1, "1");
		jedisCluster.sadd(key1, "4");
		
		// 返回集合的所有成员
		System.out.println(jedisCluster.smembers(key1));
		
		// 集合的长度
		System.out.println(jedisCluster.scard(key1));
		
		//判断 member 元素是否集合 key 的成员。
		System.out.println("是否在集合中：" + jedisCluster.sismember(key1, "1"));
		System.out.println("是否在集合中：" + jedisCluster.sismember(key1, "10"));
		
		//返回集合中的一个随机元素。
		System.out.println(jedisCluster.srandmember(key1));
		
		/**
		 * SRANDMEMBER 命令接受可选的 count 参数：
		 * count 为正数，
		 * 	如果count 小于集合的元素个数，则返回一个包含 count个元素的数组，数组中的元素各不相同。
		 * 	如果 count 大于集合的元素个数，那么返回整个集合。
		 * count 为负数，
		 * 	返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count的绝对值。
		 */
		System.out.println(jedisCluster.srandmember(key1, 3));  // [2, 1, 3]
		System.out.println(jedisCluster.srandmember(key1, 10)); // [1, 2, 3, 4]
		System.out.println(jedisCluster.srandmember(key1, -3)); // [4, 2, 4]

		// 移除并返回集合中的一个随机元素。
		for(int i = 0; i < 5; i++) {
			System.out.println(jedisCluster.spop(key1));
		}
		
		// 求两个set的交、并、差集
		jedisCluster.sadd(key1, "1", "2", "3", "4");
		jedisCluster.sadd(key2, "1", "2", "5");
		System.out.println("sinter:" + jedisCluster.sinter(key1, key2));
		System.out.println("sunion:" + jedisCluster.sunion(key1, key2));
		System.out.println("sdiff:"  + jedisCluster.sdiff(key1, key2));
		System.out.println("sdiff:"  + jedisCluster.sdiff(key2, key1));
		
		
		 /**
        	SMOVE 将元素从集合A移动到集合B，是原子性操作。
			如果 source 集合不存在或不包含指定的 member 元素，则 SMOVE 命令不执行任何操作，仅返回 0 。否则， member 元素从 source 集合中被移除，并添加到 destination 集合中去。
			当 destination 集合已经包含 member 元素时， SMOVE 命令只是简单地将 source 集合中的 member 元素删除。
			当 source 或 destination 不是集合类型时，返回一个错误。
				注：不可以在redis-cluster中直接使用SMOVE：redis.clients.jedis.exceptions.JedisClusterException: No way to dispatch this command to Redis Cluster because keys have different slots.
				解决办法可以参考mset命令，使用“{}”来将key分配到同一个slot中
         */
		System.out.println("smove:" + jedisCluster.smove(key1, key2, "3"));
		System.out.println("key1:" + jedisCluster.smembers(key1));
		System.out.println("key2:" + jedisCluster.smembers(key2));
		
	}
}
