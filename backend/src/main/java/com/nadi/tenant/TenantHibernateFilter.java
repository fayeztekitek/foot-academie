package com.nadi.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
public class TenantHibernateFilter {

    @PersistenceContext
    private EntityManager entityManager;

    public void applyFilter() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", tenantId);
    }

    public void disableFilter() {
        Session session = entityManager.unwrap(Session.class);
        try {
            session.disableFilter("tenantFilter");
        } catch (Exception ignored) {
        }
    }
}
