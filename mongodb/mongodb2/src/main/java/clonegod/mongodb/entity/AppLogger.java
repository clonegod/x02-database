package clonegod.mongodb.entity;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class AppLogger {
	
	@Id
	private String id;
	
	private String bizType;
	
	private String step;
	
	private String content;
	
	private DateTime createTime;
	
	public AppLogger() {
		super();
	}

	public AppLogger(String bizType, String step, String content, DateTime createTime) {
		super();
		this.bizType = bizType;
		this.step = step;
		this.content = content;
		this.createTime = createTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public DateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(DateTime createTime) {
		this.createTime = createTime;
	}
	
}
