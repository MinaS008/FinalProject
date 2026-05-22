package Utilities;
import java.util.ArrayList;
import java.util.List;

import Model.*;
import Controller.*;

public class Validator {
    public static final int maxNameLength = 100;
    public static final int maxDescriptionLength = 2000;
    public static final int maxListItemLength = 200;
    private static final String forbiddenChars = "/\\:*?\"<>|";

    private Validator() {
    }

    public static ValidationResult validateWorld(String name, String description) {
        List<String> errors = new ArrayList<>();

        validateName(name, "World name", errors);
        validateDescription(description, errors);

        return errors.isEmpty()
                ? ValidationResult.ok()
                : ValidationResult.fail(String.join("\n", errors));
    }

    public static ValidationResult validateEntryBase(String name, String description) {
        List<String> errors = new ArrayList<>();

        validateName(name, "Entry name", errors);
        validateDescription(description, errors);

        return errors.isEmpty()
                ? ValidationResult.ok()
                : ValidationResult.fail(String.join("\n", errors));
    }

    public static ValidationResult validateCharacter(String role) {
        if (role == null || role.isBlank()) {
            return ValidationResult.fail("Role is required for a Character.");
        }
        if (role.length() > maxListItemLength) {
            return ValidationResult.fail("Role must be under " + maxListItemLength + " characters.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateLocation(String locationType) {
        if (locationType == null || locationType.isBlank()) {
            return ValidationResult.fail("Location Type is required.");
        }
        if (locationType.length() > maxListItemLength) {
            return ValidationResult.fail("Location Type must be under " + maxListItemLength + " characters.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateItem(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return ValidationResult.fail("Item Type is required.");
        }
        if (itemType.length() > maxListItemLength) {
            return ValidationResult.fail("Item Type must be under " + maxListItemLength + " characters.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateFaction(String goal) {
        if (goal == null || goal.isBlank()) {
            return ValidationResult.fail("Goal is required for a Faction.");
        }
        if (goal.length() > maxDescriptionLength) {
            return ValidationResult.fail("Goal must be under " + maxDescriptionLength + " characters.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateListItem(String item, String fieldName) {
        if (item == null || item.isBlank()) {
            return ValidationResult.fail(fieldName + " cannot be blank.");
        }
        if (item.length() > maxListItemLength) {
            return ValidationResult.fail(fieldName + " must be under "
                    + maxListItemLength + " characters.");
        }
        return ValidationResult.ok();
    }

    //Search Validation
    public static ValidationResult validateSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return ValidationResult.fail("Search query cannot be empty.");
        }
        if (query.length() > 200) {
            return ValidationResult.fail("Search query is too long (max 200 characters).");
        }
        return ValidationResult.ok();
    }

    private static void validateName(String name, String fieldName, List<String> errors) {
        if (name == null || name.isBlank()) {
            errors.add(fieldName + " cannot be empty.");
            return; // further checks don't make sense on a blank string
        }
        if (name.length() > maxNameLength) {
            errors.add(fieldName + " must be under " + maxNameLength + " characters.");
        }
        for (char c : forbiddenChars.toCharArray()) {
            if (name.indexOf(c) >= 0) {
                errors.add(fieldName + " contains an invalid character: " + c);
                break; // report once, not once per forbidden character
            }
        }
    }

    private static void validateDescription(String description, List<String> errors) {
        if (description != null && description.length() > maxDescriptionLength) {
            errors.add("Description must be under " + maxDescriptionLength + " characters.");
        }
    }


    //Inner class
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message){
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok(){
            return new ValidationResult(true, "");
        }

        public static ValidationResult fail(String message){
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}








