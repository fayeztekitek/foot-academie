package com.nadi.service;

import com.nadi.model.AuditLog;
import com.nadi.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String action, String resource, String resourceId, String details, boolean success) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "anonymous";
        String role = auth != null && auth.getAuthorities() != null
                ? auth.getAuthorities().iterator().next().getAuthority()
                : "NONE";

        String ip = "unknown";
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
        }

        AuditLog auditLog = AuditLog.builder()
                .utilisateurEmail(email)
                .role(role)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .details(details)
                .ipAddress(ip)
                .success(success)
                .build();
        auditLogRepository.save(auditLog);
        log.info("AUDIT: {} {} {} by {} ({}) from {}", action, resource, resourceId, email, role, ip);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getByUser(String email, Pageable pageable) {
        return auditLogRepository.findByUtilisateurEmailOrderByTimestampDesc(email, pageable);
    }
}
