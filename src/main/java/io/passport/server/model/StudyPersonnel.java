package io.passport.server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * StudyPersonnel model used for the StudyPersonnel management.
 */
@Entity
@Table(name = "study_personnel")
@Getter
@Setter
@NoArgsConstructor
public class StudyPersonnel {

    @EmbeddedId
    private StudyPersonnelId id;

    @Column(name = "role")
    private String role;

    /**
     * Get the roles as a List of Strings.
     * This method parses the comma-separated roles string into a list.
     * @return List of roles
     */
    public List<String> getRolesAsList() {
        if (this.role != null && !this.role.isEmpty()) {
            return Arrays.asList(this.role.split(","));
        }
        return new ArrayList<>();
    }

    /**
     * Set the roles from a List of Strings.
     * This method joins the list into a comma-separated string and sets it as the role.
     * @param roles List of roles
     */
    public void setRolesFromList(List<String> roles) {
        this.role = String.join(",", roles);
    }
}
