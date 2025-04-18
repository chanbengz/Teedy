package com.sismics.docs.core.dao.criteria;

/**
 * User activity criteria.
 *
 * @author Claude
 */
public class UserActivityCriteria {
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Activity type.
     */
    private String activityType;
    
    /**
     * Entity ID.
     */
    private String entityId;

    public String getUserId() {
        return userId;
    }

    public UserActivityCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getActivityType() {
        return activityType;
    }

    public UserActivityCriteria setActivityType(String activityType) {
        this.activityType = activityType;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

    public UserActivityCriteria setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }
} 