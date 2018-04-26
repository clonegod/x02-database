package clonegod.mongodb.entity;

import java.util.Arrays;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Inventory {
	
	@Id
	private String id;
	private String name;
	private String code;
	private String[] tags;
	
	public Inventory() {
		super();
	}
	public Inventory(String name, String code, String[] tags) {
		super();
		this.name = name;
		this.code = code;
		this.tags = tags;
	}
	
	@Override
	public String toString() {
		return "Inventory [id=" + id + ", name=" + name + ", code=" + code + ", tags=" + Arrays.toString(tags) + "]";
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
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	
}
