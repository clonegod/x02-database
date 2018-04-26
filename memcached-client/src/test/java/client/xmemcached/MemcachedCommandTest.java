package client.xmemcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

// set, add, delete, incr/decr, flush_all, cas
public class MemcachedCommandTest {
	
	String memcachedServer = "192.168.1.201";
	int memcachedServerPort = 11211;
	
	MemcachedClient client;
	
	@Before
	public void initMemcachedClient() throws Exception {
		long start = System.currentTimeMillis();
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(String.format("%s:%d", memcachedServer, memcachedServerPort)));
		client = builder.build();
		long end = System.currentTimeMillis();
		System.out.println("init client takes: " + (end-start) + "ms");
	}
	
	@After
	public void afterMethod() throws IOException {
		client.shutdown();
	}
	
	@Test
	public void test_Storage() throws Exception {
		client.flushAll(new InetSocketAddress(memcachedServer, memcachedServerPort));
		// add - 当且仅当key不存在时，才会保存成功。
		client.add("myList", 30, Arrays.asList("memcached", "redis"));
		Object result = client.get("myList");
		System.out.println(result);
		
		client.set("myKey1", 30, "memcached");
		client.append("myKey1", " !");
		client.prepend("myKey1", "hello ");
		result = client.get("myKey1");
		System.out.println(result);
	}
	
	@Test
	public void test_cluster() {
		
	}
}
