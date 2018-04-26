package clonegod.mongodb.bestpractice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;

import clonegod.mongodb.entity.Employee;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test12MongodbUpsertOptimal {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Before
	public void setUp() {
		mongoTemplate.dropCollection(Employee.class);
	}
	
	/**
	 * save - 更新字段，需要先查询，再基于得到的对象再更新，需要发送两次请求
	 * 注意：domain Object 中必须要有id字段
	 */
	@Test
	public void testSaveByMongoTemplate() throws Exception {
		Employee emp = new Employee("alice", 0);
		mongoTemplate.save(emp);
		
		Employee emp2 = mongoTemplate.findOne(new Query(Criteria.where("name").is("alice")), Employee.class);
		emp2.setAge(18);
		mongoTemplate.save(emp2);
		
		Employee emp3 = mongoTemplate.findOne(new Query(Criteria.where("name").is("alice")), Employee.class);
		emp3.setSex("nv");
		mongoTemplate.save(emp3);
	}
	
	/**
	 * use mongoTemplate upsert - 坏处：需要明确设置哪些字段需要更新，如果字段数很多，写起来很麻烦
	 */
	@Test
	public void testUpsertByMongoTemplate() throws Exception {
		Update update1 = new Update();
		update1.set("age", 0);
		mongoTemplate.upsert(new Query(Criteria.where("name").is("alice")), update1, Employee.class);
		
		Update update2 = new Update();
		update2.set("age", 18);
		update2.set("sex", "nv");
		mongoTemplate.upsert(new Query(Criteria.where("name").is("alice")), update2, Employee.class);
		
		Update update3 = new Update();
		update3.set("age", 28);
		mongoTemplate.upsert(new Query(Criteria.where("name").is("alice")), update3, Employee.class);
		
	}
	
	/**
	 * use connection upsert - 好处：只需要传入对象，不需要设置哪些字段需要更新
	 * 
	 * 注意：domain Object 中不能存在id字段，否则插入数据库的记录的_id=null
	 */
	@Test
	public void testUpsertByConnection() throws Exception {
		persistEmployee(new Employee("alice", 0));
		
		persistEmployee(new Employee("alice", 18).setSex("nv"));
		
		persistEmployee(new Employee("alice", 18).setSex("nv").setAge(28));
	}
	
	public boolean persistEmployee(Employee employee) throws Exception {
		   
	    BasicDBObject dbObject = new BasicDBObject();
	    mongoTemplate.getConverter().write(employee, dbObject);
	    mongoTemplate.execute(Employee.class, new CollectionCallback<Object>() {
	        public Object doInCollection(DBCollection collection) throws MongoException, DataAccessException {
	            collection.update(new Query(Criteria.where("name").is(employee.getName())).getQueryObject(),
	                    dbObject,
	                    true,  // means upsert - true
	                    false  // multi update – false
	            );
	            return null;
	        }
	    });
	    return true;
	}
	
}
