package clonegod.mongodb.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Add Audit Entries to Model Objects
 * 
 */
@Document
public class Employee6 extends Audit {
	@Id
	private String id;
	
	private String name;
	
	@Override
	public String toString() {
		return "Employee6 [id=" + id + ", name=" + name + ", createdOn=" + createdOn + ", updatedOn=" + updatedOn
				+ ", version=" + version + "]";
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

}

