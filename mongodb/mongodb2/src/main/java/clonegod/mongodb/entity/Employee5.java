package clonegod.mongodb.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Automatic Deletion of Documents From Collection
 * 
 */
@Document
public class Employee5 {
	@Id
	private String id;
	
	private String name;
	
	// ttl document 2s后过期
	@Indexed(expireAfterSeconds = 2)
	
	// the document will be automatically deleted from the collection by MongoDB after 7 days from the creation date time
	//@Indexed(expireAfterSeconds = 604800)
	private LocalDateTime createdDateTime;
	
	public Employee5() {
		super();
	}

	public Employee5(String name, LocalDateTime createdDateTime) {
		super();
		this.name = name;
		this.createdDateTime = createdDateTime;
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

	public LocalDateTime getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(LocalDateTime createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	
	
}

