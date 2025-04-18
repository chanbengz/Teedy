package com.sismics.docs.core.util.jpa;

import java.util.Map.Entry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import com.sismics.util.context.ThreadLocalContext;

/**
 * Query utilities.
 *
 * @author jtremeaux 
 */
public class QueryUtil {

    /**
     * Creates a native query from the query parameters.
     * 
     * @param queryParam Query parameters
     * @return Native query
     */
    public static Query getNativeQuery(QueryParam queryParam) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery(queryParam.getQueryString());
        for (Entry<String, Object> entry : queryParam.getParameterMap().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query;
    }
    
    /**
     * Returns sorted query parameters.
     * 
     * @param queryParam Query parameters
     * @param sortCriteria Sort criteria
     * @return Sorted query parameters
     */
    public static QueryParam getSortedQueryParam(QueryParam queryParam, SortCriteria sortCriteria) {
        StringBuilder sb = new StringBuilder(queryParam.getQueryString());
        if (sortCriteria != null) {
            String queryString = queryParam.getQueryString().toLowerCase();
            
            // Determine which entity the query is for based on the FROM clause
            String orderColumn;
            if (queryString.contains("from t_user_activity ua")) {
                // User Activity queries
                switch (sortCriteria.getColumn()) {
                    case 0: orderColumn = "ua.UTA_ID_C"; break;
                    case 1: orderColumn = "ua.UTA_IDUSER_C"; break;
                    case 2: orderColumn = "u.USE_USERNAME_C"; break;
                    case 3: orderColumn = "ua.UTA_ACTIVITY_TYPE_C"; break;
                    case 4: orderColumn = "ua.UTA_ENTITY_ID_C"; break;
                    case 5: orderColumn = "d.DOC_TITLE_C"; break;
                    case 6: orderColumn = "ua.UTA_PROGRESS_N"; break;
                    case 7: orderColumn = "ua.UTA_PLANNED_DATE_D"; break;
                    case 8: orderColumn = "ua.UTA_COMPLETED_DATE_D"; break;
                    case 9: orderColumn = "ua.UTA_CREATEDATE_D"; break;
                    default: orderColumn = "ua.UTA_CREATEDATE_D";
                }
            } else if (queryString.contains("from t_tag t")) {
                // Tag queries
                switch (sortCriteria.getColumn()) {
                    case 0: orderColumn = "t.TAG_ID_C"; break;
                    case 1: orderColumn = "t.TAG_NAME_C"; break;
                    case 2: orderColumn = "t.TAG_COLOR_C"; break;
                    case 3: orderColumn = "t.TAG_IDPARENT_C"; break;
                    case 4: orderColumn = "u.USE_USERNAME_C"; break;
                    default: orderColumn = "t.TAG_NAME_C";
                }
            } else if (queryString.contains("from t_document d")) {
                // Document queries
                switch (sortCriteria.getColumn()) {
                    case 0: orderColumn = "d.DOC_ID_C"; break;
                    case 1: orderColumn = "d.DOC_TITLE_C"; break;
                    case 2: orderColumn = "d.DOC_DESCRIPTION_C"; break;
                    case 3: orderColumn = "d.DOC_CREATEDATE_D"; break;
                    case 4: orderColumn = "d.DOC_UPDATEDATE_D"; break;
                    case 5: orderColumn = "u.USE_USERNAME_C"; break;
                    default: orderColumn = "d.DOC_CREATEDATE_D";
                }
            } else {
                // Default: use an indexed column name (legacy approach)
                orderColumn = "c" + sortCriteria.getColumn();
            }
            
            sb.append(" order by ");
            sb.append(orderColumn);
            sb.append(sortCriteria.isAsc() ? " asc" : " desc");
        }
        
        return new QueryParam(sb.toString(), queryParam.getParameterMap());
    }
}
