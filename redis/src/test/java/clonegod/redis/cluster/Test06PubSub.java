package clonegod.redis.cluster;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clonegod.redis.config.RedisClusterBase;
import redis.clients.jedis.JedisPubSub;

public class Test06PubSub extends RedisClusterBase {
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
	 * 发布
	 */
	@Test
	public void testPublish() {
		jedisCluster.publish("channel-1", "hello redis cluster");
	}
	
	/**
	 * 订阅
	 */
	@Test
	public void testSubscribe() {
		// 订阅线程会一直阻塞，等待新的消息
		jedisCluster.subscribe(new JedisPubSubListener(), "channel-1", "channel-2");
	}
	
	static class JedisPubSubListener extends JedisPubSub {
	    // 取得订阅的消息后的处理  
	    public void onMessage(String channel, String message) {  
	        System.out.println(channel + "=" + message);  
	    }  

	    // 初始化订阅时候的处理  
	    public void onSubscribe(String channel, int subscribedChannels) {  
	        System.out.println(channel + "=" + subscribedChannels);  
	    }  

	    // 取消订阅时候的处理  
	    public void onUnsubscribe(String channel, int subscribedChannels) {  
	        System.out.println(channel + "=" + subscribedChannels);  
	    }  

	    // 初始化按表达式的方式订阅时候的处理  
	    public void onPSubscribe(String pattern, int subscribedChannels) {  
	        System.out.println(pattern + "=" + subscribedChannels);  
	    }  

	    // 取消按表达式的方式订阅时候的处理  
	    public void onPUnsubscribe(String pattern, int subscribedChannels) {  
	        System.out.println(pattern + "=" + subscribedChannels);  
	    }  

	    // 取得按表达式的方式订阅的消息后的处理  
	    public void onPMessage(String pattern, String channel, String message) {  
	        System.out.println(pattern + "=" + channel + "=" + message);  
	    }  
	}  
}
