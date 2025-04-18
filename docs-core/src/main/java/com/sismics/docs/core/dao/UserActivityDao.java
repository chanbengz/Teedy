package com.sismics.docs.core.dao;

import com.sismics.docs.core.dao.criteria.UserActivityCriteria;
import com.sismics.docs.core.dao.dto.UserActivityDto;
import com.sismics.docs.core.model.jpa.UserActivity;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.*;

/**
 * User activity DAO.
 * 
 * @author Claude
 */
public class UserActivityDao {
    /**
     * Creates a new user activity.
     * 
     * @param userActivity User activity
     * @return New ID
     */
    public String create(UserActivity userActivity) {
        // Create the UUID
        userActivity.setId(UUID.randomUUID().toString());
        
        // Create the user activity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        userActivity.setCreateDate(new Date());
        em.persist(userActivity);
        
        return userActivity.getId();
    }
    
    /**
     * Updates a user activity.
     * 
     * @param userActivity User activity to update
     * @return Updated user activity
     */
    public UserActivity update(UserActivity userActivity) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user activity
        UserActivity userActivityDb = em.find(UserActivity.class, userActivity.getId());
        if (userActivityDb == null) {
            return null;
        }
        
        // Update the user activity
        userActivityDb.setProgress(userActivity.getProgress());
        if (userActivity.getPlannedDate() != null) {
            userActivityDb.setPlannedDate(userActivity.getPlannedDate());
        }
        if (userActivity.getCompletedDate() != null) {
            userActivityDb.setCompletedDate(userActivity.getCompletedDate());
        }
        
        return userActivityDb;
    }
    
    /**
     * Gets a user activity by ID.
     * 
     * @param id User activity ID
     * @return User activity
     */
    public UserActivity getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(UserActivity.class, id);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Deletes a user activity.
     * 
     * @param id User activity ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the user activity
        UserActivity userActivityDb = em.find(UserActivity.class, id);
        if (userActivityDb == null) {
            return;
        }
        
        // Delete the user activity
        Date dateNow = new Date();
        userActivityDb.setDeleteDate(dateNow);
    }
    
    /**
     * Searches user activities by criteria.
     * 
     * @param paginatedList List of user activities (updated by side effects)
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     */
    public void findByCriteria(PaginatedList<UserActivityDto> paginatedList, UserActivityCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        
        StringBuilder sb = new StringBuilder("select ua.UTA_ID_C, ua.UTA_IDUSER_C, u.USE_USERNAME_C, ua.UTA_ACTIVITY_TYPE_C, ua.UTA_ENTITY_ID_C, ");
        sb.append("d.DOC_TITLE_C, ua.UTA_PROGRESS_N, ua.UTA_PLANNED_DATE_D, ua.UTA_COMPLETED_DATE_D, ua.UTA_CREATEDATE_D ");
        sb.append("from T_USER_ACTIVITY ua ");
        sb.append("join T_USER u on ua.UTA_IDUSER_C = u.USE_ID_C ");
        sb.append("left join T_DOCUMENT d on ua.UTA_ENTITY_ID_C = d.DOC_ID_C ");
        
        // Add criteria
        List<String> criteriaList = new ArrayList<>();
        criteriaList.add("ua.UTA_DELETEDATE_D is null");
        
        if (criteria.getUserId() != null) {
            criteriaList.add("ua.UTA_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        
        if (criteria.getActivityType() != null) {
            criteriaList.add("ua.UTA_ACTIVITY_TYPE_C = :activityType");
            parameterMap.put("activityType", criteria.getActivityType());
        }
        
        if (criteria.getEntityId() != null) {
            criteriaList.add("ua.UTA_ENTITY_ID_C = :entityId");
            parameterMap.put("entityId", criteria.getEntityId());
        }
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(String.join(" and ", criteriaList));
        }
        
        // Perform the search
        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
        // Assemble results
        List<UserActivityDto> userActivityDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            UserActivityDto userActivityDto = new UserActivityDto();
            userActivityDto.setId((String) o[i++]);
            userActivityDto.setUserId((String) o[i++]);
            userActivityDto.setUsername((String) o[i++]);
            userActivityDto.setActivityType((String) o[i++]);
            userActivityDto.setEntityId((String) o[i++]);
            userActivityDto.setEntityName((String) o[i++]); // Document title
            userActivityDto.setProgress((Integer) o[i++]);
            
            Timestamp plannedDate = (Timestamp) o[i++];
            if (plannedDate != null) {
                userActivityDto.setPlannedDateTimestamp(plannedDate.getTime());
            }
            
            Timestamp completedDate = (Timestamp) o[i++];
            if (completedDate != null) {
                userActivityDto.setCompletedDateTimestamp(completedDate.getTime());
            }
            
            Timestamp createDate = (Timestamp) o[i++];
            if (createDate != null) {
                userActivityDto.setCreateTimestamp(createDate.getTime());
            }
            
            userActivityDtoList.add(userActivityDto);
        }
        
        paginatedList.setResultList(userActivityDtoList);
    }
} 