package org.clarkproject.aioapi.api.obj.enums;

import lombok.Getter;

/**
 * API狀態回應
 */
@Getter
public enum ResponseStatusMessage {
    SUCCESS("success"),
    ERROR("error");
    private final String value;
    ResponseStatusMessage(String value) {
        this.value = value;
    }
}
