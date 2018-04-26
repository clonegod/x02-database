package clonegod.mongodb.bestpractice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Employee4;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test04MongodbIncludeOnlyUpdatedFields {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		mongoTemplate.remove(new Query(), Employee4.class);
	}
	
	/**
	 *  Include only Updated Fields in the Update Query
	 */
	@Test
	public void testUpdateFields() {
		mongoTemplate.insert(new Employee4("alice", 1009, 800));
		
		// bad example: This issues – 2 Queries and sends entire object to just update the salary field.
		Employee4 employee = mongoTemplate.findOne(new Query(Criteria.where("serialNumber").is(1009)), Employee4.class);
		int updatedSalary = employee.getSalary() + 1;
		employee.setSalary(updatedSalary);
		mongoTemplate.save(employee);
		
		System.out.println("----------------------");
		
		// good example: just issues one query and moreover just sends the updated field as part of the update.
		Update update = new Update();
		update.set("salary", updatedSalary);
		mongoTemplate.updateFirst(new Query(Criteria.where("serialNumber").is(1009)), update,  "Employee4");
	}
	
}
