package io.passport.server.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Audit Log Book model used to classify logs under different passports.
 */
@Entity
@Table(name = "audit_log_book")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogBook {

    @EmbeddedId
    private AuditLogBookId id;
}
