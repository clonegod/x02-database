package client.xmemcached;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

/*
memcached服务启动命令：

$> /opt/memcached/bin/memcached -d -u nobody -vv -m 128 -n 48 -f 1.25  -l 192.168.1.201 -p 11211

*/
public class XmemcachedConsistentHashTest {
	MemcachedClient client;
	
	
	
	String memcachedServerList = 
			"192.168.1.201:11211,192.168.1.201:11212 192.168.1.201:11213,192.168.1.201:11214";
	
	@Before
	public void initMemcachedClient() throws Exception {
		long start = System.currentTimeMillis();
		int[] weights = new int[] {1, 2};
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddressMap(memcachedServerList), weights);
		builder.setSessionLocator(new KetamaMemcachedSessionLocator());
		builder.setFailureMode(true);
		client = builder.build();
		long end = System.currentTimeMillis();
		System.out.println("init client takes: " + (end-start) + "ms");
	}
	
	@After
	public void afterMethod() throws IOException {
		client.shutdown();
	}
	
	@Test
	public void testClusterAndFailure() throws Exception {
		client.flushAll();
		for(int i=1; i<=100; i++) {
			client.add("key"+i, 600, "test cluster"+i);
		}
		
		System.out.println("add....done");
		
		while(true) {
			// 手动shutdown某个主节点，测试是否使用了standby节点。
			Thread.sleep(5000);
			System.out.println("==========================");
			for(int i=1; i<=10; i++) {
				String key = "key"+i;
				Object value = client.get(key);
				System.out.println(key+"="+value);
			}
		}
	}
	
}
