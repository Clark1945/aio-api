package org.clarkproject.aioapi.api.repository;

import org.clarkproject.aioapi.api.obj.AccessLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccessLogRepository extends MongoRepository<AccessLog, String> {

}
