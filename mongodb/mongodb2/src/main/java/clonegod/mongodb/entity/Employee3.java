package clonegod.mongodb.entity;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Compound Index - Try to use covered indexes if possible.
 * 
 */
@Document
@CompoundIndex(name = "code_name_idx", def = "{'code' : 1, 'name' : 1}" )
public class Employee3 {

	private String name;
	private String code;
	
	public Employee3() {
		super();
	}
	public Employee3(String name, String code) {
		super();
		this.name = name;
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}

