package clonegod.mongodb.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@TypeAlias("employee")
public class Employee {

	// 使用connection 的upsert时，不要在domain object中设置id属性
//	@Id
//	private String id;
	
	private String name;
	
	private int age;
	
	private String sex;

	public Employee(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}

	public Employee() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public Employee setAge(int age) {
		this.age = age;
		return this;
	}

	public String getSex() {
		return sex;
	}

	public Employee setSex(String sex) {
		this.sex = sex;
		return this;
	}

}
