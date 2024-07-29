package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Credentials pair to pass the values in to the requests.
 */
@AllArgsConstructor
@NoArgsConstructor
public class Credentials {
    public String username;
    public String password;
}
