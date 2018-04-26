package clonegod.mongodb.entity;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Automatic index creation is only done for types annotated with @Document.
 * 
 * --- Single Field Index
 */
@Document
public class Employee1 {
	
	@Id
	private String id;
	
	@Indexed  //by default the index direction is ASCENDING
	private Long employeeId;

	@Indexed(direction = IndexDirection.DESCENDING)
	private DateTime enrolledDateTime;

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public DateTime getEnrolledDateTime() {
		return enrolledDateTime;
	}

	public void setEnrolledDateTime(DateTime enrolledDateTime) {
		this.enrolledDateTime = enrolledDateTime;
	}
	
	
}
