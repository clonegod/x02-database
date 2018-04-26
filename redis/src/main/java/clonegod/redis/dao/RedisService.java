package clonegod.redis.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

	// inject the actual template
	@Autowired
	private RedisTemplate<String, String> stringRedisTemplate;


	/**
	 * 清除集群数据
	 */
	public void flushAll() {
		stringRedisTemplate.getConnectionFactory().getClusterConnection().flushAll();
	}

	/**
	 * 队列操作 - 入队
	 * 	push all 底层通过pipeline进行批量操作
	 */
	public void addLinks(String myqueue, String[] urls) {
		stringRedisTemplate.boundListOps(myqueue).leftPushAll(urls);
	}

	/**
	 * 队列操作 - 取队列所有元素
	 */
	public List<String> getAllLink(String myqueue) {
		return stringRedisTemplate.boundListOps(myqueue).range(0, -1);
	}

}
