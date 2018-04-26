package clonegod.mongodb.bestpractice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Employee6;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test09MongodbAudit {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		mongoTemplate.remove(new Query(), Employee6.class);
	}
	
	@Test
	public void testAutoSetAuditEntityAttribute() throws Exception {
		Employee6 emp = new Employee6();
		emp.setName("alice");
		mongoTemplate.insert(emp);
		
		Employee6 empFind = mongoTemplate.findOne(new Query(), Employee6.class);
		
		Assert.assertNotNull(empFind.getCreatedOn());
		Assert.assertNotNull(empFind.getUpdatedOn());
		System.out.println(empFind);
	}
	
}
