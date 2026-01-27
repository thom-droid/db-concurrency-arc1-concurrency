package com.example.simplebooking.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public void log(String msg) {
        Audit audit = new Audit();
        audit.setMsg(msg);
        audit.setCreatedAt(Timestamp.from(Instant.now()));
        auditRepository.save(audit);
    }
}
