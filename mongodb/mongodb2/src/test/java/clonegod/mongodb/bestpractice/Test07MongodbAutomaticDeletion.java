package clonegod.mongodb.bestpractice;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Employee5;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test07MongodbAutomaticDeletion {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		
	}
	
	/**
	 * 先插入数据，再执行另一个test，验证数据被自动删除
	 */
	@Test
	public void testInsert() {
		mongoTemplate.remove(new Query(), Employee5.class);
		for(int i=0; i<10; i++) {
			mongoTemplate.insert(new Employee5("alice", LocalDateTime.now()));
		}
	}
	
	/**
	 * Automatic Deletion of Documents From Collection
	 */
	@Test
	public void testTTLCollection() throws Exception {
		Thread.sleep(3000);
		while(true) {
			long count = mongoTemplate.count(new Query(), Employee5.class);
			System.out.println("count=" + count);
			if(count == 0) {
				break;
			}
			Thread.sleep(3000);
		}
	}
	
}
