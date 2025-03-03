package io.passport.server.model;

/**
 * Enumerable for dynamic description creation.
 */
public enum Description {
    CREATION("Creation of "),
    DELETION("Deletion of "),
    UPDATE("Update of ");

    private final String body;

    Description(String body) {
        this.body = body;
    }

    public String getDescription(String affectedRelation, String affectedRecordId) {
        return body + affectedRelation + " with id " + affectedRecordId;
    }
}