package clonegod.mongodb.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Include only Updated Fields in the Update Query
 * 
 */
@Document
public class Employee4 {
	@Id
	private String id;
	
	private String name;
	private int serialNumber;
	private int salary;
	
	public Employee4() {
		super();
	}
	public Employee4(String name, int serialNumber, int salary) {
		super();
		this.name = name;
		this.serialNumber = serialNumber;
		this.salary = salary;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}
	public int getSalary() {
		return salary;
	}
	public void setSalary(int salary) {
		this.salary = salary;
	}
	
	
	
}

