package org.clarkproject.aioapi.api.service;

import org.clarkproject.aioapi.api.obj.AccessLog;
import org.clarkproject.aioapi.api.repository.AccessLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;
    public AccessLogService(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    public void insertAccessLog(AccessLog accessLog) {
        accessLogRepository.save(accessLog);
    }
}
