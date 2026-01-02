package io.passport.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO to collect Cascade Authorization results
 *
 * @param status Number representing the authorization status of Cascades (1=Authorized, 0=Not Authorized)
 * @param tables A comma-separated String containing the names of the tables involved (Empty String = No Cascade Occurred)
 */
public record ValidationResult(boolean status, String tables) {

    /**
     * Aggregates a list of validation results into a single result while eliminating duplicate Cascades
     *
     * @param results The list of validation results to aggregate.
     * @return A single ValidationResult containing the combined status and distinct table list.
     */
    public static ValidationResult aggregate(List<ValidationResult> results) {
        if (results == null || results.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean isOverallAuthorized = true;

        for (ValidationResult res : results) {
            if (!res.status()) {
                isOverallAuthorized = false;
                break;
            }
        }

        List<String> collectedTables = new ArrayList<>();

        for (ValidationResult res : results) {
            boolean shouldCollect = isOverallAuthorized || (!res.status());

            if (shouldCollect && res.tables() != null && !res.tables().isEmpty()) {
                collectedTables.add(res.tables());
            }
        }

        String finalString = collectedTables.stream()
                .distinct()
                .collect(Collectors.joining(","));

        return new ValidationResult(isOverallAuthorized, finalString);
    }
}