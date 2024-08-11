package org.clarkproject.aioapi.api.obj.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class APIResponse {
    private String status;
    private String message;
    @Schema(name = "Response Body",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Object info;
    public APIResponse(String status, String message, Object info) {
        this.status = status;
        this.message = message;
        this.info = info;
    }
    public APIResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
