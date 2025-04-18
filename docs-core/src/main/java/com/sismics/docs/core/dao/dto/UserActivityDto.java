package com.sismics.docs.core.dao.dto;

/**
 * User activity DTO.
 *
 * @author Claude
 */
public class UserActivityDto {
    /**
     * Activity ID.
     */
    private String id;
    
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * User name.
     */
    private String username;
    
    /**
     * Activity type.
     */
    private String activityType;
    
    /**
     * Entity ID.
     */
    private String entityId;
    
    /**
     * Entity name.
     */
    private String entityName;
    
    /**
     * Progress percentage (0-100).
     */
    private Integer progress;
    
    /**
     * Planned date timestamp.
     */
    private Long plannedDateTimestamp;
    
    /**
     * Completed date timestamp.
     */
    private Long completedDateTimestamp;
    
    /**
     * Creation date timestamp.
     */
    private Long createTimestamp;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Long getPlannedDateTimestamp() {
        return plannedDateTimestamp;
    }

    public void setPlannedDateTimestamp(Long plannedDateTimestamp) {
        this.plannedDateTimestamp = plannedDateTimestamp;
    }

    public Long getCompletedDateTimestamp() {
        return completedDateTimestamp;
    }

    public void setCompletedDateTimestamp(Long completedDateTimestamp) {
        this.completedDateTimestamp = completedDateTimestamp;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
} 