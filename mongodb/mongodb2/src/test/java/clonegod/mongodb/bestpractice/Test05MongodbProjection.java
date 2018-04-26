package clonegod.mongodb.bestpractice;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Inventory;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test05MongodbProjection {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		mongoTemplate.remove(new Query(), Inventory.class);
		
		mongoTemplate.insert(new Inventory("Adapter", "A012", new String[] {"a","b","c"}));
	}
	
	/**
	 * Use Projections to Reduce the Amount of Data Returned
	 */
	@Test
	public void testProjections() {
		Query query = new Query(Criteria.where("name").is("Adapter"));
		query.fields().include("code").include("tags");
		List<Inventory> inventoryList = mongoTemplate.find(query,  Inventory.class);
		inventoryList.forEach(x -> System.out.println(x));
	}
	
}
