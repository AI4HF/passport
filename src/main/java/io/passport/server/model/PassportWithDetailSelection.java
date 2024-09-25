package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model for passport with details selection
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassportWithDetailSelection {

    private Passport passport;

    private PassportDetails passportDetailsSelection;
}
