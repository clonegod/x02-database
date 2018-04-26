package clonegod.redis.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
/**
 * 1.基于连接池方式实现对五个数据类型操作，每种数据类型2个操作
 * 
 * @author clonegod
 *
 */
public class JedisClientUseConnPoolTest {

	private static final String HOST = "192.168.1.103";
	private static final int PORT = 6379;
	private static final int TIMEOUT = 2000;

	private JedisPool jedisPool;

	@Before
	public void initialize() {
		GenericObjectPoolConfig poolConf = new JedisPoolConfig();
		poolConf.setMaxTotal(3000);
		poolConf.setBlockWhenExhausted(true);
		poolConf.setMaxWaitMillis(5000);
		poolConf.setTestOnBorrow(true);
		poolConf.setMaxIdle(200);
		poolConf.setMinIdle(100);
		poolConf.setMinEvictableIdleTimeMillis(60000);
		poolConf.setTimeBetweenEvictionRunsMillis(30000);

		jedisPool = new JedisPool(poolConf, HOST, PORT, TIMEOUT);
		
		jedisPool.getResource().flushAll();
	}

	@After
	public void destory() {
		if (!jedisPool.isClosed()) {
			jedisPool.destroy();
		}
	}

	@Test
	public void checkConnections() {
		Jedis jedis = jedisPool.getResource();
		// jedis.auth("foobared");
		jedis.set("foo", "bar");
		assertEquals("bar", jedis.get("foo"));
		jedis.close();
		jedisPool.destroy();
		assertTrue(jedisPool.isClosed());
	}

	@Test
	public void testString() {
		Jedis jedis = jedisPool.getResource();
		
		// multiGet
		Transaction tx = jedis.multi();
		
		tx.set("name", "zhangsan");
		tx.set("age", "20");
		
		tx.exec();
		
		List<String> values = jedis.mget("name", "age");
		assertTrue(values.contains("zhangsan"));
		assertTrue(values.contains("20"));
		
		// increment
		tx = jedis.multi();
		
		tx.set("id", "1");
		tx.incrBy("id", 1L);
		
		tx.exec();
		
		String value = jedis.get("id");
		assertEquals("2", value);
		
	}

	@Test
	public void testList() {
		Jedis jedis = jedisPool.getResource();
		
		String[] values = {"1","2","3","4"};
		
		// 队列-先进先出
		jedis.lpush("key1", "1","2","3","4");
		
		List<String> retRanged = jedis.lrange("key1", 0, 3);
		assertEquals("[4, 3, 2, 1]", Arrays.toString(retRanged.toArray()));
		
		assertEquals("1", jedis.rpop("key1"));
		assertEquals("2", jedis.rpop("key1"));
		assertEquals("3", jedis.rpop("key1"));
		assertEquals("4", jedis.rpop("key1"));
		
		
		// 堆栈-先进后出
		jedis.rpush("key2", values);
		
		List<String> retRanged2 = jedis.lrange("key2", 0, 3);
		assertEquals("[1, 2, 3, 4]", Arrays.toString(retRanged2.toArray()));
		
		assertEquals("4", jedis.rpop("key2"));
		assertEquals("3", jedis.rpop("key2"));
		assertEquals("2", jedis.rpop("key2"));
		assertEquals("1", jedis.rpop("key2"));
		
	
	}

	@Test
	public void testSet() {
		Jedis jedis = jedisPool.getResource();
		
		String[] members = {"22","33","22","44","55","66"};
		jedis.sadd("key1", members);
		
		long size = jedis.scard("key1");
		assertEquals(size,5);

		jedis.srem("key1", "22");
		assertFalse(jedis.sismember("key1", "22"));
	
	}

	@Test
	public void testZSet() {
		Jedis jedis = jedisPool.getResource();
		
		Map<String,Double> scoreMembers = new HashMap<>();
		scoreMembers.put("11", 10d);
		scoreMembers.put("22", 9d);
		scoreMembers.put("33", 14d);
		scoreMembers.put("44", 6d);
		scoreMembers.put("55", 50d);
		
		Transaction tx = jedis.multi();
		tx.zadd("1", scoreMembers);
		tx.exec();
		
		Set<String> rangedatas = jedis.zrange("1", 0L, 1L);
		assertTrue(rangedatas.contains("44"));
		
		jedis.zadd("1", 40, "44");
		Set<String> rangedatas1 = jedis.zrange("1", 0L, 1L);
		assertFalse(rangedatas1.contains("44"));
		
		Set<String> datasbyScore = jedis.zrangeByScore("1", 40d, 50d);
		
		assertEquals(datasbyScore.size(),2);//44和55
		assertTrue(datasbyScore.contains("44"));
		assertTrue(datasbyScore.contains("55"));
	
	}

	@Test
	public void testHash() {
		Jedis jedis = jedisPool.getResource();
		
		Map<String, String> user = new HashMap<String, String>();
		user.put("id", "111");
		user.put("name", "liubx");
		user.put("money", "10000");
		user.put("other", "other");
		jedis.hmset("firstKey", user);

		Map<String, String> userget = jedis.hgetAll("firstKey");
		assertEquals(userget.get("name"), "liubx");

		jedis.hincrBy("firstKey", "money", 100);
		String money = (String) jedis.hget("firstKey", "money");
		assertEquals(Long.parseLong(money), 10100L);

		String otherValue = (String) jedis.hget("firstKey", "other");
		assertEquals(otherValue, "other");

		jedis.hdel("firstKey", "other");
		String otherValue2 = (String) jedis.hget("firstKey", "other");
		assertNull(otherValue2);
	}

}
