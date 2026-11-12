package com.homefit.server.service;

import com.homefit.server.domain.AuditLog;
import com.homefit.server.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLoggerService {

    private final AuditLogRepository auditLogRepository;

    public AuditLoggerService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String adminEmail, String action, String entityName, String entityId, String detail) {
        try {
            AuditLog log = new AuditLog();
            log.setAdminEmail(adminEmail != null && !adminEmail.isBlank() ? adminEmail : "admin@homefit.com");
            log.setAction(action);
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setDetail(detail);
            auditLogRepository.save(log);
        } catch (Exception ignored) {
        }
    }
}
