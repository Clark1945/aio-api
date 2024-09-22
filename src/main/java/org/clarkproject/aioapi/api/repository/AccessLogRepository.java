package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.obj.RequestAccessLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessLogRepository extends MongoRepository<RequestAccessLog, String> {
    List<RequestAccessLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
