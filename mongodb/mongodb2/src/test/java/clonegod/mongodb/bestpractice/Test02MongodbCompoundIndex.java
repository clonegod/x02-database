package clonegod.mongodb.bestpractice;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Employee2;
import clonegod.mongodb.util.MongoDBUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test02MongodbCompoundIndex {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		mongoTemplate.remove(new Query(), Employee2.class);
	}
	
	@Test
	public void testCompoundIndex() {
		List<Employee2> list = new ArrayList<>();
		for(int i=0; i< 100; i++) {
			Employee2 emp2 = new Employee2();
			int serialNumber = ThreadLocalRandom.current().nextInt(100);
			LocalDateTime startDateTime = LocalDateTime.now();
			LocalDateTime endDateTime = startDateTime.plusYears(1);
			emp2.setSerialNumber(serialNumber);
			emp2.setStartDateTime(startDateTime);
			emp2.setEndDateTime(endDateTime);
			list.add(emp2);
		}
		mongoTemplate.insertAll(list);
		
		Employee2 target = list.get(5);
		
		Query query = new Query(
				new Criteria().andOperator(
					Criteria.where("serialNumber").is(target.getSerialNumber()), 
					new Criteria().orOperator(
				        new Criteria().andOperator(
				        		// 需要转换DateTime为Date类型
				        	Criteria.where("startDateTime").gte(Date.from(target.getStartDateTime().atZone(ZoneId.systemDefault()).toInstant())), 
				        	Criteria.where("startDateTime").lt(Date.from(target.getEndDateTime().atZone(ZoneId.systemDefault()).toInstant()))
				        )
					)
				)
			);
		
		MongoDBUtil.performExplainQuery(mongoTemplate, query, "employee2");
	}
	
}
