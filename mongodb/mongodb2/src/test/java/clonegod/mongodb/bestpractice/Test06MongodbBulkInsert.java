package clonegod.mongodb.bestpractice;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Inventory;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test06MongodbBulkInsert {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		mongoTemplate.remove(new Query(), Inventory.class);
		
	}
	
	/**
	 * Use bulk insert instead of individual inserts
	 */
	@Test
	public void testBulkOperations() {
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkMode.ORDERED, Inventory.class);
		bulkOps.insert(
				Arrays.asList(
						new Inventory("abc", "001", new String[] {"x1"}),
						new Inventory("def", "002", new String[] {"y1","y2"}),
						new Inventory("xyz", "003", new String[] {"z1","z2","z3"})
				));
		bulkOps.execute();
	}
	
}
