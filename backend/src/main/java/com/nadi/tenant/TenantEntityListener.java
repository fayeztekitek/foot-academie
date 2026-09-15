package com.nadi.tenant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.lang.reflect.Field;

public class TenantEntityListener {

    @PrePersist
    public void setTenantId(Object entity) {
        setTenantField(entity);
    }

    @PreUpdate
    public void updateTenantId(Object entity) {
        setTenantField(entity);
    }

    private void setTenantField(Object entity) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) return;

        try {
            Field field = findTenantField(entity.getClass());
            if (field != null) {
                field.setAccessible(true);
                if (field.get(entity) == null) {
                    field.set(entity, tenantId);
                }
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    private Field findTenantField(Class<?> clazz) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField("tenantId");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
