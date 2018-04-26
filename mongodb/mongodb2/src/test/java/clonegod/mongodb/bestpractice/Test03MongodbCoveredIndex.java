package clonegod.mongodb.bestpractice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Employee3;
import clonegod.mongodb.util.MongoDBUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test03MongodbCoveredIndex {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		mongoTemplate.remove(new Query(), Employee3.class);
	}
	
	/**
	 * Use Covered Indexes
	 */
	@Test
	public void testCoveredIndex() {
		mongoTemplate.insert(new Employee3("alice", "abc"));
		mongoTemplate.insert(new Employee3("bob", "ijk"));
		mongoTemplate.insert(new Employee3("cindy", "xyz"));
		mongoTemplate.insert(new Employee3("doug", "111"));
		
		Query query = new Query(Criteria.where("code").in("abc","ijk", "xyz"));
		query.fields().include("name");
		
		MongoDBUtil.performExplainQuery(mongoTemplate, query, "employee3");
	}
	
}
