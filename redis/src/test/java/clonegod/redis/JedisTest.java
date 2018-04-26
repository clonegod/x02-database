package clonegod.redis;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;

public class JedisTest {
	
	static Jedis jedis;
	
	static final String MASTER = "192.168.1.203";
	static final int PORT = 6379;
	
	@BeforeClass
	public static void setUp() {
		jedis = new Jedis(MASTER, PORT);
	}
	
	@AfterClass
	public static void tearDown() {
		jedis.close();
	}
	
	@Test
	public void test() {
		System.out.println(jedis.dbSize());
	}
	
}
