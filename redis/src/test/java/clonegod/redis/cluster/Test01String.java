package clonegod.redis.cluster;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clonegod.redis.config.RedisClusterBase;

public class Test01String extends RedisClusterBase {
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
	 * 简单字符串的读写
	 */
	@Test
	public void testStringData() {
		System.out.println(jedisCluster.set(TEST_KEY, "string data"));
		System.out.println(jedisCluster.get(TEST_KEY));
	}
	
	/**
	 * 设置key值，并返回旧值
	 * @throws IOException 
	 */
	@Test
	public void testGetOldSetNew() throws IOException {
		System.out.println(jedisCluster.set(TEST_KEY, "old string data"));
		System.out.println(jedisCluster.get(TEST_KEY));
		
		System.out.println(jedisCluster.getSet(TEST_KEY, "new string data"));
		System.out.println(jedisCluster.get(TEST_KEY));
	}
	
	/**
	 * 追加字符串，其返回值是追加后的字符串长度
	 */
	@Test
	public void testAppend() {
		System.out.println(jedisCluster.set(TEST_KEY, "value"));
		
		System.out.println(jedisCluster.append(TEST_KEY, "-v1"));
		System.out.println(jedisCluster.get(TEST_KEY));
		
		System.out.println(jedisCluster.append(TEST_KEY, "-v2"));
		System.out.println(jedisCluster.get(TEST_KEY));
	}
	
	
	/**
	 * key不存在，才会设置成功
	 */
	@Test
	public void testSetNx() {
		System.out.println(jedisCluster.setnx(TEST_KEY, "string data")); //key不存在，返回值为1
		System.out.println(jedisCluster.get(TEST_KEY));
		
		System.out.println(jedisCluster.setnx(TEST_KEY, "string data")); //已经存在，返回值为0
		System.out.println(jedisCluster.get(TEST_KEY));
	}
	
	/**
	 * key 过期时间/超时时间
	 */
	@Test
	public void testExpire() throws InterruptedException {
		System.out.println(jedisCluster.setex(TEST_KEY, 3, "string data")); //超时时间单位：秒
		for(int i = 0; i < 5; i++) {
			System.out.println(jedisCluster.get(TEST_KEY));
			Thread.sleep(1000);
		}
	}
	
	/**
	 * 子字符串的操作
	 */
	@Test
	public void testStringRange() {
		System.out.println(jedisCluster.set(TEST_KEY, "alice@123.com")); 
		System.out.println(jedisCluster.get(TEST_KEY));
		
		// 更新子串
		System.out.println(jedisCluster.setrange(TEST_KEY, 5, "#0123.com")); // 返回替换后的字符串长度
		System.out.println(jedisCluster.get(TEST_KEY));
		
		
		// 返回子串(包头包尾)
		System.out.println(jedisCluster.getrange(TEST_KEY, 0, 4));
		
	}
	
	/**
     * 批量操作key
     * keySlot算法中，如果key包含{}，就会使用第一个{}内部的字符串作为hash key，因此，只要保证所有key的第一个{}内使用相同字符串，就能将这些key分配到相同slot。 
     * 参考  http://brandnewuser.iteye.com/blog/2314280
     * redis.clients.util.JedisClusterCRC16#getSlot(java.lang.String)
     *
     * 注意：这样的话，本来可以hash到不同的slot中的数据都放到了同一个slot中，所以使用的时候要注意数据不要太多导致一个slot数据量过大，数据分布不均匀！
     *
     * MSET 是一个原子性(atomic)操作，所有给定 key 都会在同一时间内被设置，某些给定 key被更新而另一些 key没有改变的情况，是不可能发生的。
     */
	@Test
	public void testMset() {
		/**
		 * 直接往集群进行批量添加，会报错。原因是：不同的key被分配到了不同的slot
		 * jedisCluster.mset("key1", "value1", "key2", "value2");
		 * redis.clients.jedis.exceptions.JedisClusterException: No way to dispatch this command to Redis Cluster because keys have different slots.
		 */
		
		String result = jedisCluster.mset(
					String.format("{%s}:%s", TEST_KEY_PREFIX, "name"), "Alice" ,
					String.format("{%s}:%s", TEST_KEY_PREFIX, "age"),  "23" ,
					String.format("{%s}:%s", TEST_KEY_PREFIX, "sex"),  "female"
				);
		System.out.println(result);
		
		String name = jedisCluster.get(String.format("{%s}:%s", TEST_KEY_PREFIX, "name"));
		System.out.println(name);
		
		String age = jedisCluster.get(String.format("{%s}:%s", TEST_KEY_PREFIX, "age"));
		System.out.println(age);
		
		String sex = jedisCluster.get(String.format("{%s}:%s", TEST_KEY_PREFIX, "sex"));
		System.out.println(sex);
		
		List<String> results = jedisCluster.mget(
									String.format("{%s}:%s", TEST_KEY_PREFIX, "name"),
									String.format("{%s}:%s", TEST_KEY_PREFIX, "age"),
									String.format("{%s}:%s", TEST_KEY_PREFIX, "sex")
								);
		
		for(String str : results) {
			System.out.print(str + "\t");
		}
	}
	
	/**
	 * setnx - 当且仅当key不存在时，才能成功设置
	 */
	@Test
	public void testMsetNX() throws InterruptedException {
		// 批量删除
		jedisCluster.del(
				String.format("{%s}:%s", TEST_KEY_PREFIX, "name"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "age"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "sex")
				);
		System.out.println(jedisCluster.mget(
				String.format("{%s}:%s", TEST_KEY_PREFIX, "name"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "age"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "sex")
				));
		
		// 批量设置 - 成功返回1
		System.out.println(jedisCluster.msetnx(
				String.format("{%s}:%s", TEST_KEY_PREFIX, "name"), "Alice" ,
				String.format("{%s}:%s", TEST_KEY_PREFIX, "age"),  "24" ,
				String.format("{%s}:%s", TEST_KEY_PREFIX, "sex"),  "female"
			));
		System.out.println(jedisCluster.mget(
									String.format("{%s}:%s", TEST_KEY_PREFIX, "name"),
									String.format("{%s}:%s", TEST_KEY_PREFIX, "age"),
									String.format("{%s}:%s", TEST_KEY_PREFIX, "sex")
								));
		
		
		// 批量设置 - 失败返回0
		System.out.println(jedisCluster.msetnx(
				String.format("{%s}:%s", TEST_KEY_PREFIX, "name"), "Alice" ,
				String.format("{%s}:%s", TEST_KEY_PREFIX, "phone"),  "13012345678" ,
				String.format("{%s}:%s", TEST_KEY_PREFIX, "address"),  "beijing"
			));
		System.out.println(jedisCluster.mget(
				String.format("{%s}:%s", TEST_KEY_PREFIX, "name"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "age"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "sex"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "phone"),
				String.format("{%s}:%s", TEST_KEY_PREFIX, "address")
				));
	}
	
	
	/**
	 * 多线程并发incr操作，测试线程安全性
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
	 */
	@Test
    public void testNumberIncr() throws InterruptedException {
		final String numKey = "incrNum";
        jedisCluster.del(numKey);
        
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        
        int nThreads = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(nThreads);
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
        executorService.prestartAllCoreThreads();
        
        for(int i = 0 ; i < nThreads ; i ++){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //每个线程加1000次，每次加1
                    for(int j = 0 ; j < 1000 ; j ++){
                        atomicInteger.incrementAndGet();
                        jedisCluster.incr(numKey);
                    }
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        
        System.out.println(atomicInteger);
        
        System.out.println(jedisCluster.get(numKey));
        
    }
	
}
