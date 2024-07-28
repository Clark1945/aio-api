package org.clarkproject.aioapi.api.obj;

import lombok.Getter;

@Getter
public enum ResponseStatusMessage {
    SUCCESS("success"),
    ERROR("error");
    private final String value;
    ResponseStatusMessage(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
