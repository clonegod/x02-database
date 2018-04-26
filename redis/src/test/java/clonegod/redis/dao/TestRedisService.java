package clonegod.redis.dao;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRedisService {
	
	@Autowired
	private RedisService redisService;
	
	private static final String QUEUE_USER = "QUEUE_USER:uid001";
	
	StopWatch sw;
	
	@Before
	public void beforeMethod() {
		sw = new StopWatch();
		sw.start();
	}
	
	@After
	public void afterMethod() {
		sw.stop();
		System.out.println("take time in millis :" + sw.getTotalTimeMillis());
	}
	
	

	@Test
	public void testQueuePut() throws MalformedURLException {
		redisService.flushAll();
		
		List<String> urls = new ArrayList<>();
		for(int i = 0; i < 10000; i++) {
			urls.add("http://localhost:8080/redis/"+i);
		}
		redisService.addLinks(QUEUE_USER, urls.toArray(new String[0]));
	}
	
	@Test
	public void testQueueGet() {
		List<String> urls = redisService.getAllLink(QUEUE_USER);
		System.out.println("queue size: " + urls.size());
	}
	
}
