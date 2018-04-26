package clonegod.mongodb.entity;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Compound Index
 * 
 * 	Compound index needs to be constructed with fields in the following order:
 *		1、 Fields involved in Equality criteria
 *		2、 Fields involved in Range criteria
 *	复合索引的构造声明，先写等值比较的索引字段，再写范围比较的索引字段。
 *
 *	Another important piece of information is that:
 *		when manually applying a sort the fields in the sort method must appear in the same order as the index 
 *		and can only sort on the original sort order or it’s inverse.
 *	另一个重点要的点：指定的排序规则必须与声明复合索引时自动的排序规则相同，或者全部相反。
 *	
 */
@Document
@CompoundIndex(name = "slNo_dt_idx",def = "{'serialNumber' : 1, 'startDateTime' : 1, 'endDateTime' : 1}" )
public class Employee2 {

	private int serialNumber;
	
	private LocalDateTime  startDateTime;
	
	private LocalDateTime  endDateTime;

	public int getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(int serialNumber) {
		this.serialNumber = serialNumber;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

}

