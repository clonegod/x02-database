package clonegod.redis.cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import clonegod.redis.config.RedisClusterBase;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.ZParams.Aggregate;

public class Test05Zset extends RedisClusterBase {
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
	 * sortedSet - 按分值自动排序的set集合
	 *  元素从2个维度进行查询：
	 *  	1、按score分值区间获取成员member
	 * 		2、按rank排名/元素索引区间范围来获取成员member
	 */
	@Test
	public void testSortedSet() {
		String kye = "SORTED_SET_KEY";
		
		jedisCluster.del(kye);
		
		/** 添加元素 */
		System.out.println(jedisCluster.zadd(kye, 100.00, "alice"));
		System.out.println(jedisCluster.zadd(kye, 88.00, "bob"));
		System.out.println(jedisCluster.zadd(kye, 80.00, "cindy"));
		System.out.println(jedisCluster.zadd(kye, 99.00, "dubbo"));
		System.out.println(jedisCluster.zadd(kye, 80.00, "ela"));
		
		//返回有序集 key 的数量。
		System.out.println("zcard:" + jedisCluster.zcard(kye)); 
		
		
		
		// ------> 按score分值查询member
		//获取成员的score
		System.out.println("zscore:" + jedisCluster.zscore(kye, "bob"));
		System.out.println("zscore:" + jedisCluster.zscore(kye, "bob2")); // 不存在的元素，返回score为null
		
		//返回有序集 key 中score 在某个范围的数量。
		System.out.println("zcount:" + jedisCluster.zcount(kye, 80, 90)); 
		
		
		/** 查询元素 */
		/**
		 * zrangeByScore - score区间内，升序排序
		 *  返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
		 *  有序集成员按 score 值递增(从小到大)次序排列。
		 *  具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。
		 */
		System.out.println("zrangeByScore:" + jedisCluster.zrangeByScore(kye, 80, 90));
		
		
		
		// ------> 按rank排名查询member
		/**
		 * zrank	获取元素按score升序排序后，在集合中的排名
		 * 	返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。rank从0开始。
		 * 
		 * zrevrank 逆序后的排名
		 * 	返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从大到小)顺序排列。rank从0开始。
		 */
		System.out.println(jedisCluster.zrank(kye, "alice"));
		System.out.println(jedisCluster.zrevrank(kye, "alice"));
		
		
		/**
		 * zrange - 索引区间内，升序排序
		 * 	返回有序集 key中，指定区间内的成员。并按score从小到大排序
		 * zrevrange - 索引区间内，逆序排序
		 *  返回有序集 key 中，指定区间内的成员。按score从大到小 	
		 */
		System.out.println("zrange:" + jedisCluster.zrange(kye, 0, -1)); // 升序
		System.out.println("zrange 最后两名:" + jedisCluster.zrange(kye, 0, 1)); 
		System.out.println("zrevrange:" + jedisCluster.zrevrange(kye, 0, -1)); // 降序
		System.out.println("zrevrange 前两名:" + jedisCluster.zrevrange(kye, 0, 1)); 
		
		/**
		 * zrangeWithScores - 索引区间内，返回member+score
		 *  返回有序集 key 中，指定区间内的成员。按score从小到大.返回值包含score
		 */
		Set<Tuple> tuples = jedisCluster.zrangeWithScores(kye, 0, -1);
		for(Tuple tup : tuples) {
			System.out.println(tup.getElement() + ":" + tup.getScore());
		}
		
		/** 删除元素 */
		// 按成员删除. 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。
		System.out.println(jedisCluster.zrange(kye, 0, -1));
		System.out.println(jedisCluster.zrem(kye, "ela", "fred"));
		System.out.println(jedisCluster.zrange(kye, 0, -1));
		
		// 按下标删除
		System.out.println(jedisCluster.zrange(kye, 0, -1));
		System.out.println(jedisCluster.zremrangeByRank(kye, 2, 2));
		System.out.println(jedisCluster.zrange(kye, 0, -1));
		
		// 按分值删除
		System.out.println(jedisCluster.zrange(kye, 0, -1));
		System.out.println(jedisCluster.zremrangeByScore(kye, 0, 85));
		System.out.println(jedisCluster.zrange(kye, 0, -1));
		
		
		/**=======================================================================================*/
		/*
        	接下来这几个操作，需要使用"{}"使得key落到同一个slot中才可以
         */
		System.out.println("-------");
		String key1 = String.format("{%s}:%s", TEST_KEY_PREFIX, "k1");
		String key2 = String.format("{%s}:%s", TEST_KEY_PREFIX, "k2");
		String key3 = String.format("{%s}:%s", TEST_KEY_PREFIX, "k3");
		jedisCluster.del(key1, key2, key3);
		
        Map<String, Double> scoreMembers1 = new HashMap<>();
		scoreMembers1.put("alice", 100.00);
		scoreMembers1.put("bob", 88.00);
		scoreMembers1.put("cindy", 80.00);
		
		Map<String, Double> scoreMembers2 = new HashMap<>();
		scoreMembers2.put("alice", 60.00);
		scoreMembers2.put("bob", 50.00);
		scoreMembers2.put("cindy", 30.00);
		scoreMembers2.put("ela", 80.00);
		
		// zadd一次添加多个元素，需要使用"{}"使得key落到同一个slot中才可以
        System.out.println(jedisCluster.zadd(key1, scoreMembers1));
        System.out.println(jedisCluster.zadd(key2, scoreMembers2));
        
        System.out.println("zrange: "+jedisCluster.zrange(key1, 0, -1));
        System.out.println("zrange: "+jedisCluster.zrange(key2, 0, -1));
        
        
        /*
        ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]
		计算给定的一个或多个有序集的并集，其中给定 key 的数量必须以 numkeys 参数指定，并将该并集(结果集)储存到 destination 。
		默认情况下，结果集中某个成员的 score 值是所有给定集下该成员 score 值之 和 。
		WEIGHTS
			使用 WEIGHTS 选项，你可以为 每个 给定有序集 分别 指定一个乘法因子(multiplication factor)，每个给定有序集的所有成员的 score 值在传递给聚合函数(aggregation function)之前都要先乘以该有序集的因子。
			如果没有指定 WEIGHTS 选项，乘法因子默认设置为 1 。
		AGGREGATE
			使用 AGGREGATE 选项，你可以指定并集的结果集的聚合方式。
			默认使用的参数 SUM ，可以将所有集合中某个成员的 score 值之 和 作为结果集中该成员的 score 值；使用参数 MIN ，可以将所有集合中某个成员的 最小 score 值作为结果集中该成员的 score 值；而参数 MAX 则是将所有集合中某个成员的 最大 score 值作为结果集中该成员的 score 值。
         */
        System.out.println("zunionstore: "+jedisCluster.zunionstore(key3, 
        															new ZParams().aggregate(Aggregate.MAX), 
        															key1, key2)); //合并keyA和keyB并保存到keyC中
		for(Tuple tup : jedisCluster.zrangeWithScores(key3, 0, -1)) {
			System.out.println(tup.getElement() + ":" + tup.getScore());
		}
        
        System.out.println("zinterstore: "+jedisCluster.zinterstore(key3, key1, key2)); //交集
        System.out.println("zrange: "+jedisCluster.zrange(key3, 0, -1));
	}
}
