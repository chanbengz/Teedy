package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.UserActivityDao;
import com.sismics.docs.core.dao.criteria.UserActivityCriteria;
import com.sismics.docs.core.dao.dto.UserActivityDto;
import com.sismics.docs.core.model.jpa.UserActivity;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User activity REST resources.
 * 
 * @author Claude
 */
@Path("/useractivity")
public class UserActivityResource extends BaseResource {
    /**
     * Returns all user activities (admin only).
     *
     * @api {get} /useractivity Get user activities
     * @apiName GetUserActivities
     * @apiGroup UserActivity
     * @apiParam {String} [activity_type] Activity type filter
     * @apiParam {String} [user_id] User ID filter
     * @apiParam {Number} [limit] Maximum number of activities to return
     * @apiParam {Number} [offset] First activity to return
     * @apiParam {String} [sort_column] Column to sort by
     * @apiParam {Boolean} [asc] If true, sort in ascending order
     * @apiSuccess {Object[]} activities List of activities
     * @apiSuccess {String} activities.id Activity ID
     * @apiSuccess {String} activities.user_id User ID
     * @apiSuccess {String} activities.username Username
     * @apiSuccess {String} activities.activity_type Activity type
     * @apiSuccess {String} activities.entity_id Entity ID
     * @apiSuccess {String} activities.entity_name Entity name
     * @apiSuccess {Number} activities.progress Progress percentage (0-100)
     * @apiSuccess {Number} activities.planned_date_timestamp Planned date timestamp (optional)
     * @apiSuccess {Number} activities.completed_date_timestamp Completed date timestamp (optional)
     * @apiSuccess {Number} activities.create_timestamp Creation date timestamp
     * @apiSuccess {Number} total Total number of activities
     * @apiPermission admin
     * @apiVersion 1.0.0
     * 
     * @param limit Maximum number of activities to return
     * @param offset First activity to return
     * @param sortColumn Column to sort by
     * @param asc If true, sort in ascending order
     * @param activityType Activity type filter
     * @param userId User ID filter
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("activity_type") String activityType,
            @QueryParam("user_id") String userId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Initialize the sort criteria
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        
        // Initialize the criteria
        UserActivityCriteria criteria = new UserActivityCriteria();
        if (activityType != null) {
            criteria.setActivityType(activityType);
        }
        if (userId != null) {
            criteria.setUserId(userId);
        }
        
        // Get the activities
        PaginatedList<UserActivityDto> paginatedList = PaginatedLists.create(limit, offset);
        UserActivityDao userActivityDao = new UserActivityDao();
        userActivityDao.findByCriteria(paginatedList, criteria, sortCriteria);

        // Build the response
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder activities = Json.createArrayBuilder();

        for (UserActivityDto userActivityDto : paginatedList.getResultList()) {
            JsonObjectBuilder activity = Json.createObjectBuilder()
                    .add("id", userActivityDto.getId())
                    .add("user_id", userActivityDto.getUserId())
                    .add("username", userActivityDto.getUsername())
                    .add("activity_type", userActivityDto.getActivityType())
                    .add("progress", userActivityDto.getProgress());
            
            if (userActivityDto.getEntityId() != null) {
                activity.add("entity_id", userActivityDto.getEntityId());
            }
            
            if (userActivityDto.getEntityName() != null) {
                activity.add("entity_name", userActivityDto.getEntityName());
            }
            
            if (userActivityDto.getPlannedDateTimestamp() != null) {
                activity.add("planned_date_timestamp", userActivityDto.getPlannedDateTimestamp());
            }
            
            if (userActivityDto.getCompletedDateTimestamp() != null) {
                activity.add("completed_date_timestamp", userActivityDto.getCompletedDateTimestamp());
            }
            
            if (userActivityDto.getCreateTimestamp() != null) {
                activity.add("create_timestamp", userActivityDto.getCreateTimestamp());
            }
            
            activities.add(activity);
        }
        
        response.add("activities", activities)
                .add("total", paginatedList.getResultCount());
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns user activities for the current user.
     *
     * @api {get} /useractivity/user Get current user's activities
     * @apiName GetUserActivitiesForCurrentUser
     * @apiGroup UserActivity
     * @apiParam {String} [activity_type] Activity type filter
     * @apiParam {String} [entity_id] Entity ID filter (document, file, etc.)
     * @apiParam {Number} [limit] Maximum number of activities to return
     * @apiParam {Number} [offset] First activity to return
     * @apiParam {String} [sort_column] Column to sort by
     * @apiParam {Boolean} [asc] If true, sort in ascending order
     * @apiSuccess {Object[]} activities List of activities
     * @apiSuccess {String} activities.id Activity ID
     * @apiSuccess {String} activities.user_id User ID
     * @apiSuccess {String} activities.username Username
     * @apiSuccess {String} activities.activity_type Activity type
     * @apiSuccess {String} activities.entity_id Entity ID
     * @apiSuccess {String} activities.entity_name Entity name
     * @apiSuccess {Number} activities.progress Progress percentage (0-100)
     * @apiSuccess {Number} activities.planned_date_timestamp Planned date timestamp (optional)
     * @apiSuccess {Number} activities.completed_date_timestamp Completed date timestamp (optional)
     * @apiSuccess {Number} activities.create_timestamp Creation date timestamp
     * @apiSuccess {Number} total Total number of activities
     * @apiPermission user
     * @apiVersion 1.0.0
     * 
     * @param limit Maximum number of activities to return
     * @param offset First activity to return
     * @param sortColumn Column to sort by
     * @param asc If true, sort in ascending order
     * @param activityType Activity type filter
     * @param entityId Entity ID filter
     * @return Response
     */
    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentUserActivities(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("activity_type") String activityType,
            @QueryParam("entity_id") String entityId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Initialize the sort criteria
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        
        // Initialize the criteria
        UserActivityCriteria criteria = new UserActivityCriteria();
        criteria.setUserId(principal.getId()); // Always filter by current user
        
        if (activityType != null) {
            criteria.setActivityType(activityType);
        }
        if (entityId != null) {
            criteria.setEntityId(entityId);
        }
        
        // Get the activities
        PaginatedList<UserActivityDto> paginatedList = PaginatedLists.create(limit, offset);
        UserActivityDao userActivityDao = new UserActivityDao();
        userActivityDao.findByCriteria(paginatedList, criteria, sortCriteria);

        // Build the response
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder activities = Json.createArrayBuilder();

        for (UserActivityDto userActivityDto : paginatedList.getResultList()) {
            JsonObjectBuilder activity = Json.createObjectBuilder()
                    .add("id", userActivityDto.getId())
                    .add("user_id", userActivityDto.getUserId())
                    .add("username", userActivityDto.getUsername())
                    .add("activity_type", userActivityDto.getActivityType())
                    .add("progress", userActivityDto.getProgress());
            
            if (userActivityDto.getEntityId() != null) {
                activity.add("entity_id", userActivityDto.getEntityId());
            }
            
            if (userActivityDto.getEntityName() != null) {
                activity.add("entity_name", userActivityDto.getEntityName());
            }
            
            if (userActivityDto.getPlannedDateTimestamp() != null) {
                activity.add("planned_date_timestamp", userActivityDto.getPlannedDateTimestamp());
            }
            
            if (userActivityDto.getCompletedDateTimestamp() != null) {
                activity.add("completed_date_timestamp", userActivityDto.getCompletedDateTimestamp());
            }
            
            if (userActivityDto.getCreateTimestamp() != null) {
                activity.add("create_timestamp", userActivityDto.getCreateTimestamp());
            }
            
            activities.add(activity);
        }
        
        response.add("activities", activities)
                .add("total", paginatedList.getResultCount());
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Create or update a user activity.
     *
     * @api {put} /useractivity Create or update a user activity
     * @apiName PutUserActivity
     * @apiGroup UserActivity
     * @apiParam {String} id Activity ID (for update only)
     * @apiParam {String} activity_type Activity type
     * @apiParam {String} [entity_id] Entity ID (document, file, etc.)
     * @apiParam {String} [planned_date] Planned date (format: yyyy-MM-dd)
     * @apiParam {Number} progress Progress percentage (0-100)
     * @apiSuccess {String} id Activity ID
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission user
     * @apiVersion 1.0.0
     * 
     * @param id Activity ID (for update only)
     * @param activityType Activity type
     * @param entityId Entity ID
     * @param plannedDate Planned date
     * @param progress Progress
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrUpdate(
            @FormParam("id") String id,
            @FormParam("activity_type") String activityType,
            @FormParam("entity_id") String entityId,
            @FormParam("planned_date") String plannedDate,
            @FormParam("progress") Integer progress) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        if (activityType == null) {
            throw new ClientException("ValidationError", "Activity type is required");
        }
        ValidationUtil.validateRequired(progress, "progress");
        ValidationUtil.validateRequired(activityType, "activity_type");
        
        // Get the authenticated user
        String userId = principal.getId();
        
        UserActivityDao userActivityDao = new UserActivityDao();
        UserActivity userActivity;
        
        // Update or create
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (id != null) {
                // Update
                userActivity = userActivityDao.getById(id);
                if (userActivity == null) {
                    throw new ClientException("ActivityNotFound", "Activity not found");
                }
                
                // Check if this user activity belongs to the current user
                if (!userActivity.getUserId().equals(userId)) {
                    throw new ForbiddenClientException();
                }
                
                // Update the user activity
                userActivity.setProgress(progress);
                if (plannedDate != null) {
                    userActivity.setPlannedDate(dateFormat.parse(plannedDate));
                }
                
                // If progress is 100%, set completed date
                if (progress == 100) {
                    userActivity.setCompletedDate(new Date());
                } else {
                    userActivity.setCompletedDate(null);
                }
                
                userActivityDao.update(userActivity);
            } else {
                // Create
                userActivity = new UserActivity();
                userActivity.setUserId(userId);
                userActivity.setActivityType(activityType);
                userActivity.setEntityId(entityId);
                userActivity.setProgress(progress);
                if (plannedDate != null) {
                    userActivity.setPlannedDate(dateFormat.parse(plannedDate));
                }
                
                // If progress is 100%, set completed date
                if (progress == 100) {
                    userActivity.setCompletedDate(new Date());
                }
                
                id = userActivityDao.create(userActivity);
            }
        } catch (Exception e) {
            throw new ClientException("ValidationError", e.getMessage());
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a user activity.
     *
     * @api {delete} /useractivity/:id Delete a user activity
     * @apiName DeleteUserActivity
     * @apiGroup UserActivity
     * @apiParam {String} id Activity ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Activity not found
     * @apiPermission user
     * @apiVersion 1.0.0
     * 
     * @param id Activity ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the activity
        UserActivityDao userActivityDao = new UserActivityDao();
        UserActivity userActivity = userActivityDao.getById(id);
        
        if (userActivity == null) {
            throw new ClientException("NotFound", "Activity not found");
        }
        
        // Check if the current user has admin rights or is the owner of the activity
        if (!principal.getId().equals(userActivity.getUserId()) && !hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        
        // Delete the activity
        userActivityDao.delete(id);
        
        // Always return OK status
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        
        return Response.ok().entity(response.build()).build();
    }
} 