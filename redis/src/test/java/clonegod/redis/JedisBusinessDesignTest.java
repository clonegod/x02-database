package clonegod.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

import clonegod.redis.model.User;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;

/**
 * 结合redis的多种数据结构，对数据的存储与高效查询进行设计
 * 1、使用redis，一方面是要直接利用其高效的读写性能。
 * 2、但是，应该更进一步，有效利用各种数据结构的特点，将它们整合起来实现更复杂的业务功能。---灵活应用！
 * 
 * hash - 
 * 	一般用来存储原始对象数据
 * 
 * set/zset - 
 * 	一般，redis是根据key获取value，如果需要根据value来过滤查询，该怎么做?
 * 	使用set集合将字段值（具体值，或者值的某个范围）作为set的键，将字段key作为set的value进行存储。
 * 	维护二级索引（根据value反向构建对key的引用）
 * 	使用set提供的交集、并集功能来实现多条件关系的组合，交集可以提供类似AND的语义，并集可提供OR的语义。
 *
 */
public class JedisBusinessDesignTest {

	static Jedis jedis;
	
	static final String MASTER = "192.168.1.203";
	static final int PORT = 6379;
	
	// 业务数据表 - redis hash
	static final String SYS_USER_TABLE 		= "SYS_USER_TABLE";
	
	// 二级索引结构 - redis set/zset
	static final String SYS_USER_SEL_AGE_LT_30 = "SYS_USER_SEL_AGE_LT_30";
	static final String SYS_USER_SEL_AGE_GE_30 = "SYS_USER_SEL_AGE_GE_30";
	static final String SYS_USER_SEL_SEX_MAN 	= "SYS_USER_SEL_SEX_MAN";
	static final String SYS_USER_SEL_SEX_WOMAN 	= "SYS_USER_SEL_SEX_WOMAN";
	
	// zset 交集
	static final String SYS_USER_SEL_AGE_LT_30_AND_SEX_MAN = "SYS_USER_SEL_AGE_LT_30_AND_SEX_MAN";
	static final String SYS_USER_SEL_AGE_GE_30_AND_SEX_WOMAN = "SYS_USER_SEL_AGE_GE_30_AND_SEX_WOMAN";
	
	@BeforeClass
	public static void setUp() {
		jedis = new Jedis(MASTER, PORT);
		
		String[] oldKeys = jedis.keys("SYS_USER_*").toArray(new String[0]);
		if(oldKeys.length > 0) {
			jedis.del(oldKeys);
		}
		
		// 初始化数据
		for(User user : users()) {
			// 1. 将原始数据存入HASH结构中
			String userId = user.getId();
			jedis.hset(SYS_USER_TABLE, userId, JSON.toJSONString(user));
			
			// 2. 针对不同维度的查询字段构建索引，使用set集合存储hash结构中的key
			// 2.1 构建年龄的索引
			if(user.getAge() < 30) {
//				jedis.sadd(SYS_USER_SEL_AGE_LT_30, userId);
				jedis.zadd(SYS_USER_SEL_AGE_LT_30, user.getAge(), userId); // 将age作为score存储到zset中
			} else {
//				jedis.sadd(SYS_USER_SEL_AGE_GE_30, userId);
				jedis.zadd(SYS_USER_SEL_AGE_GE_30, user.getAge(), userId); // 将age作为score存储到zset中
			}
			// 2.2 构建性别的索引
			if("m".equals(user.getSex())) {
				jedis.sadd(SYS_USER_SEL_SEX_MAN, userId);
			} else {
				jedis.sadd(SYS_USER_SEL_SEX_WOMAN, userId);
			}
		}
	}
	
	@AfterClass
	public static void tearDown() {
		jedis.close();
	}
	
	/**
	 * 查询条件：age < 30
	 */
	@Test
	public void testQuerySingleCondition() {
//		Set<String> agelt30Set = jedis.smembers(SYS_USER_SEL_AGE_LT_30);
		Set<String> agelt30Set = jedis.zrange(SYS_USER_SEL_AGE_LT_30, 0, -1);
		List<String> agelt30List = jedis.hmget(SYS_USER_TABLE, agelt30Set.toArray(new String[0]));
		for (Iterator<String> iterator = agelt30List.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			User user = JSON.parseObject(string, User.class);
			System.out.println(user);
		}
	}
	
	/**
	 * 查询条件：age < 30 & sex = MAN
	 */
	@Test
	public void testQueryMultiCondition() {
		jedis.zinterstore(SYS_USER_SEL_AGE_LT_30_AND_SEX_MAN, 
				SYS_USER_SEL_AGE_LT_30, SYS_USER_SEL_SEX_MAN);
		Set<String> ageSexSet = jedis.zrange(SYS_USER_SEL_AGE_LT_30_AND_SEX_MAN, 0, -1);
		List<String> ageSexList = jedis.hmget(SYS_USER_TABLE, ageSexSet.toArray(new String[0]));
		for (Iterator<String> iterator = ageSexList.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			User user = JSON.parseObject(string, User.class);
			System.out.println(user);
		}
	}
	
	/**
	 * 查询条件：age = 30 & sex = WOMAN
	 */
	@Test
	public void testQueryMultiEqualCondition() {
		// 对age>30的集合 和 sex=woman的集合，取交集
		jedis.zinterstore(SYS_USER_SEL_AGE_GE_30_AND_SEX_WOMAN, 
				new ZParams().aggregate(ZParams.Aggregate.MAX), // 不同集合的交集对score字段的处理方式：max,min,sum
				SYS_USER_SEL_AGE_GE_30, SYS_USER_SEL_SEX_WOMAN);
		
		// 对上一步得到的交集结果集，再筛选出age=30的member
		double min = 30;
		double max = 30;
		Set<Tuple> ageSexSet = jedis.zrangeByScoreWithScores(SYS_USER_SEL_AGE_GE_30_AND_SEX_WOMAN, min, max);
		List<String> ageList = new ArrayList<>();
		for(Tuple tup : ageSexSet) {
			ageList.add(tup.getElement());
		}
		
		// 使用已过滤出的key，作为hash存储中field，取得对应的value 
		List<String> ageSexList = jedis.hmget(SYS_USER_TABLE, ageList.toArray(new String[0]));
		for (Iterator<String> iterator = ageSexList.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			User user = JSON.parseObject(string, User.class);
			System.out.println(user);
		}
	}
	
	private static Set<User> users() {
		User u1 = new User(UUID.randomUUID().toString(), "alice", 20, "w");
		User u2 = new User(UUID.randomUUID().toString(), "bob",   22, "m");
		User u3 = new User(UUID.randomUUID().toString(), "cindy", 38, "w");
		User u4 = new User(UUID.randomUUID().toString(), "dug",   25, "m");
		User u5 = new User(UUID.randomUUID().toString(), "ella",  30, "w");
		User u6 = new User(UUID.randomUUID().toString(), "frank", 25, "m");
				
		Set<User> users = new HashSet<>();
		users.addAll(Arrays.asList(u1, u2, u3, u4, u5, u6)) ;
		return users;
	}
}
