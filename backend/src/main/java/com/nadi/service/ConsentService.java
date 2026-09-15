package com.nadi.service;

import com.nadi.dto.ConsentLogResponse;
import com.nadi.model.ConsentLog;
import com.nadi.model.Parent;
import com.nadi.repository.ConsentLogRepository;
import com.nadi.repository.ParentRepository;
import com.nadi.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentLogRepository consentLogRepository;
    private final ParentRepository parentRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public ConsentLogResponse record(Long parentId, String type, boolean granted, String details, String ipAddress) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé: " + parentId));

        ConsentLog log = ConsentLog.builder()
                .parent(parent)
                .type(ConsentLog.ConsentType.valueOf(type))
                .granted(granted)
                .details(details)
                .ipAddress(ipAddress)
                .tenantId(securityUtils.getCurrentTenantId())
                .build();

        return toResponse(consentLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<ConsentLogResponse> getAll() {
        return consentLogRepository.findAllByOrderByDateConsentementDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsentLogResponse> getByParent(Long parentId) {
        return consentLogRepository.findByParentIdOrderByDateConsentementDesc(parentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        long total = consentLogRepository.count();
        long granted = consentLogRepository.findAll().stream()
                .filter(ConsentLog::isGranted).count();
        return Map.of("total", total, "granted", granted, "refused", total - granted);
    }

    @Transactional
    public void markExported(List<Long> ids) {
        for (Long id : ids) {
            consentLogRepository.findById(id).ifPresent(c -> {
                c.setExported(true);
                consentLogRepository.save(c);
            });
        }
    }

    private ConsentLogResponse toResponse(ConsentLog c) {
        return ConsentLogResponse.builder()
                .id(c.getId())
                .parentId(c.getParent().getId())
                .parentNom(c.getParent().getNom())
                .parentPrenom(c.getParent().getPrenom())
                .parentEmail(c.getParent().getEmail())
                .type(c.getType().name())
                .granted(c.isGranted())
                .details(c.getDetails())
                .ipAddress(c.getIpAddress())
                .exported(c.isExported())
                .dateConsentement(c.getDateConsentement())
                .build();
    }
}
