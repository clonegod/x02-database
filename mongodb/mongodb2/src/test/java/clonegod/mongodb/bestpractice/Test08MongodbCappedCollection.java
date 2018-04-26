package clonegod.mongodb.bestpractice;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.AppLogger;
import clonegod.mongodb.util.MongoDBUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test08MongodbCappedCollection {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
	}
	
	/**
	 * 先插入数据，再执行另一个test，验证数据被自动删除
	 */
	@Test
	public void testCappedCollection() throws Exception {
		if(! mongoTemplate.collectionExists(AppLogger.class)) {
			MongoDBUtil.createCappedCollections(mongoTemplate, AppLogger.class);
		}
		
		Assert.assertTrue(mongoTemplate.getCollection("appLogger").isCapped());
		
		for(int i=0; i<100; i++) {
			mongoTemplate.insert(new AppLogger("Shopping", "Cart", "Add to xxx to cart", DateTime.now()));
		}
		
		long count = mongoTemplate.count(new Query(), AppLogger.class);
		Assert.assertEquals(5, count);
	}
	
}
