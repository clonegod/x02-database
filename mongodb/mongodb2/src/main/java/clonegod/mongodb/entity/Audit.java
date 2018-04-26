package clonegod.mongodb.entity;

import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

/**
 * Add Audit Entries to Model Objects
 * 
 */
public class Audit {
    @CreatedDate
    protected DateTime createdOn;
    
    @LastModifiedDate
    protected DateTime updatedOn;
    
    /* Enable Optimistic Locking on Write Operations */
    @Version
    protected Long version;
    
    public DateTime getCreatedOn() {
        return createdOn;
    }public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }
    public DateTime getUpdatedOn() {
        return updatedOn;
    }
    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
}