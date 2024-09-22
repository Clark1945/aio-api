package org.clarkproject.aioapi.api.service;

import org.clarkproject.aioapi.api.obj.RequestAccessLog;
import org.clarkproject.aioapi.api.repository.AccessLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;
    private final RequestAccessLogRepository requestAccessLogRepository;
    public AccessLogService(AccessLogRepository accessLogRepository, RequestAccessLogRepository requestAccessLogRepository) {
        this.accessLogRepository = accessLogRepository;
        this.requestAccessLogRepository = requestAccessLogRepository;
    }

    public void insertAccessLog(RequestAccessLog requestAccessLog) {
        accessLogRepository.save(requestAccessLog);
    }

    public List<RequestAccessLog> getAccessWithInTenMinutes(LocalDateTime start, LocalDateTime end) {
        if(start == null) {
            start = LocalDateTime.now().minusMinutes(10);
        }
        if(end == null) {
            end = LocalDateTime.now();
        }

        return accessLogRepository.findByTimestampBetween(start,end);
    }

    public void backUpAccessLog(List<RequestAccessLog> requestAccessLogList) {

    }
}
