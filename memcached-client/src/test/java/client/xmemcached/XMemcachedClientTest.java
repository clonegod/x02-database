package client.xmemcached;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.transcoders.StringTranscoder;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class XMemcachedClientTest {
	
	MemcachedClient client;
	
	String memcachedServer11211 = "192.168.1.201:11211";
	
	@Before
	public void initMemcachedClient() throws Exception {
		long start = System.currentTimeMillis();
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(memcachedServer11211));
//		builder.setCommandFactory(new BinaryCommandFactory()); // 使用二进制协议
		client = builder.build();
		long end = System.currentTimeMillis();
		System.out.println("init client takes: " + (end-start) + "ms");
	}
	
	@After
	public void afterMethod() throws IOException {
		client.shutdown();
	}
	
	/**
	 * set	设置
	 * get	获取
	 * get...timeout	带超时的获取
	 * touch	重新设置超时时间
	 */
	@Test
	public void test1() throws Exception {
		Map<String,String> userInfo = new HashMap<>();
		userInfo.put("name", "alice");
		userInfo.put("age", "18");
		
		client.set("key", 3600, userInfo); //同步存储value到memcached，缓存超时为1小时，3600秒。
		
		Object someObject = client.get("key"); //从memcached获取key对应的value
		System.out.println(someObject);
		
		someObject=client.get("key", 2000); //从memcached获取key对应的value,操作超时2秒
		System.out.println(someObject);
		
		boolean success=client.touch("key",10); //更新缓存的超时时间为10秒。
		System.out.println(success);
		
		//client.delete("key"); //删除value
	}
	
	@Test
	public void test2() throws Exception {
		client.flushAll();
		if (!client.set("hello", 0, "world")) {
			System.err.println("set error");
		}
		if (client.add("hello", 0, "dennis")) {
			System.err.println("Add error,key is existed");
		}
		if (!client.replace("hello", 0, "dennis")) {
			System.err.println("replace error");
		}
		client.append("hello", " good");
		client.prepend("hello", "hello ");
		String name = client.get("hello", new StringTranscoder());
		System.out.println(name);

		client.deleteWithNoReply("hello");
	}
}
