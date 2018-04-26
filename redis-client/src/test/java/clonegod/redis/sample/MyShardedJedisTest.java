package clonegod.redis.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Before;
import org.junit.Test;

import dataguru.redis.sample02.HostAndPortUtil;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

public class MyShardedJedisTest {

	private List<JedisShardInfo> shards;

	@Before
	public void startUp() {
		shards = new ArrayList<JedisShardInfo>();
		shards.add(new JedisShardInfo("192.168.1.103", 6379));
		shards.add(new JedisShardInfo("192.168.1.103", 6380));
//		shards.get(0).setPassword("foobared");
//		shards.get(1).setPassword("foobared");

		Jedis j1 = new Jedis(shards.get(0));
		j1.connect();
		j1.flushAll();
		j1.disconnect();

		Jedis j2 = new Jedis(shards.get(1));
		j2.connect();
		j2.flushAll();
		j2.disconnect();

	}

	@Test
	public void checkConnections() {
		ShardedJedisPool pool = new ShardedJedisPool(new GenericObjectPoolConfig(), shards);
		ShardedJedis jedis = pool.getResource();
		jedis.set("foo", "bar");
		assertEquals("bar", jedis.get("foo"));
		jedis.close();
		pool.destroy();
	}
	
	@Test
	public void testList() {
		ShardedJedisPool pool = new ShardedJedisPool(new GenericObjectPoolConfig(), shards);
		ShardedJedis jedis = pool.getResource();
		
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
		
	}

	@Test
	public void testSet() {
		ShardedJedisPool pool = new ShardedJedisPool(new GenericObjectPoolConfig(), shards);
		ShardedJedis jedis = pool.getResource();
		
		String[] members = {"22","33","22","44","55","66"};
		jedis.sadd("key1", members);
		
		long size = jedis.scard("key1");
		assertEquals(size,5);

		jedis.srem("key1", "22");
		assertFalse(jedis.sismember("key1", "22"));
	
		jedis.sadd("key2", members);
	}
	
	/**
	 * redis集群不支持密码，所以Redis服务不能设置password！！！
	 */
	@Test
	public void clusterInsert() {
		Set<HostAndPort> nodes = new HashSet<>();
		nodes.addAll(HostAndPortUtil.getClusterServers());
		JedisCluster jc = new JedisCluster(nodes);
		Random r = new Random();
		for(int i=0; i<10000; i++)
			jc.set(r.nextInt()+"", r.nextInt()+"");
	}

}
