package clonegod.mongodb.bestpractice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.entity.Employee6;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test10MongodbOptimisticLocking {
	@Autowired
	private MongoTemplate mongoTemplate; 
	
	@Before
	public void setUp() {
		mongoTemplate.remove(new Query(), Employee6.class);
	}
	
	/**
	 * Enable Optimistic Locking on Write Operations
	 */
	@Test
	public void testUpdateWithOptimisticLocking() throws Exception {
		Employee6 emp = new Employee6();
		emp.setName("alice");
		mongoTemplate.insert(emp);
		
		CyclicBarrier barrier = new CyclicBarrier(30);
		CountDownLatch cdl = new CountDownLatch(30);
		
		for(int i=0; i<30; i++) {
			final int j = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						barrier.await(); // 控制线程同时执行任务
						while(true) {
							Employee6 alice = null;
							try {
								alice = mongoTemplate.findOne(new Query(Criteria.where("name").regex("alice.*")), Employee6.class);
								alice.setName("alice"+j);
								mongoTemplate.save(alice);
								break;
							} catch (OptimisticLockingFailureException e) {
								System.err.println("Optimistic lock exception on saving entity." + "\tRetry--------------->" + alice.getName());
								Thread.sleep(50);
								continue;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						cdl.countDown();
					}
				}
			}).start();
		}
		
		cdl.await(); // 等待其它线程的任务都执行完毕，防止test提前退出
	}
	
}
