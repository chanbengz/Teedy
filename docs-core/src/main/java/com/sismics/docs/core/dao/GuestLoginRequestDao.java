package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.GuestLoginRequest;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

public class GuestLoginRequestDao {
    public String create(GuestLoginRequest request) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        if (request.getId() == null) {
            request.setId(UUID.randomUUID().toString());
        }
        em.persist(request);
        return request.getId();
    }

    public List<GuestLoginRequest> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<GuestLoginRequest> query = em.createQuery("SELECT g FROM GuestLoginRequest g ORDER BY g.timestamp DESC", GuestLoginRequest.class);
        return query.getResultList();
    }

    public void updateStatus(String id, String status) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        GuestLoginRequest request = em.find(GuestLoginRequest.class, id);
        if (request != null) {
            request.setStatus(status);
        }
    }

    public GuestLoginRequest findByToken(String token) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<GuestLoginRequest> query = em.createQuery("SELECT g FROM GuestLoginRequest g WHERE g.token = :token", GuestLoginRequest.class);
        query.setParameter("token", token);
        List<GuestLoginRequest> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public GuestLoginRequest findById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(GuestLoginRequest.class, id);
    }
}