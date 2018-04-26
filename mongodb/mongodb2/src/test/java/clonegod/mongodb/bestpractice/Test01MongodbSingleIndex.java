package clonegod.mongodb.bestpractice;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Employee1;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test01MongodbSingleIndex {
	
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
//		mongoTemplate.dropCollection(Employee1.class); // 删除集合，以及集合上的所有索引
		mongoTemplate.remove(new Query(), Employee1.class);
	}
	
	@Test
	public void testSingleIndex() {
		Employee1 emp1 = new Employee1();
		emp1.setEmployeeId(100L);
		emp1.setEnrolledDateTime(new DateTime());
		mongoTemplate.insert(emp1);
	}
	
}
