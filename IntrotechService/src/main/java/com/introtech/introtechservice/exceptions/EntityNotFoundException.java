package com.introtech.introtechservice.exceptions;

public class EntityNotFoundException extends IntrotechException {
    public EntityNotFoundException(String entityName) {
        super(entityName + " not found");
    }
}
