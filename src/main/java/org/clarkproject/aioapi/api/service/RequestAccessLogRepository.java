package org.clarkproject.aioapi.api.service;

import org.clarkproject.aioapi.api.obj.RequestAccessLog;
import org.springframework.data.repository.CrudRepository;

public interface RequestAccessLogRepository extends CrudRepository<RequestAccessLog,String> {
}
