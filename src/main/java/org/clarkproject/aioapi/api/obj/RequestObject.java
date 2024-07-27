package org.clarkproject.aioapi.api.obj;

import org.clarkproject.aioapi.api.tool.ValidationException;

public abstract class RequestObject {
    abstract void  validate() throws ValidationException;
}
