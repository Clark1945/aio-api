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
//@Entity(name = "access_log")
public class RequestAccessLog {
    private final String requestLog;
    private final String responseLog;
    private final LocalDateTime timestamp;
}
