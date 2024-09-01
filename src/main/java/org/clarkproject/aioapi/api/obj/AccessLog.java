package org.clarkproject.aioapi.api.obj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@ToString
@Document(collection = "access_log")
public class AccessLog {
    private final String requestLog;
    private final String responseLog;
    private final LocalDateTime timestamp;
}
