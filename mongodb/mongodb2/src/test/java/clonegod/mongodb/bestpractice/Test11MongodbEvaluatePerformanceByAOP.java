package clonegod.mongodb.bestpractice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import clonegod.mongodb.dao.CustomerRepository;
import clonegod.mongodb.entity.Customer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test11MongodbEvaluatePerformanceByAOP {
	@Autowired
	private CustomerRepository repository;
	
	@Before
	public void setUp() {
		repository.deleteAll();

		// save a couple of customers
		repository.save(new Customer("Alice", "Smith"));
		repository.save(new Customer("Bob", "Smith"));
	}
	
	/**
	 * Evaluate the Performance of each DAO Methods using Spring AOP
	 */
	@Test
	public void testEvaluateQueryPerformance() throws Exception {
		// fetch all customers
		System.out.println("Customers found with findAll():");
		System.out.println("-------------------------------");
		for (Customer customer : repository.findAll()) {
			System.out.println(customer);
		}
		System.out.println();

		// fetch an individual customer
		System.out.println("Customer found with findByFirstName('Alice'):");
		System.out.println("--------------------------------");
		System.out.println(repository.findByFirstName("Alice"));
		System.out.println();

		System.out.println("Customers found with findByLastName('Smith'):");
		System.out.println("--------------------------------");
		for (Customer customer : repository.findByLastName("Smith")) {
			System.out.println(customer);
		}
	}
	
}
