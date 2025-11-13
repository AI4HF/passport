package io.passport.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Passport PDF generation request DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PdfRequest {
    private String htmlContent;
    private String baseUrl;
    private String fileName;
    private Boolean landscape;
    private String width;
    private String height;
    private String studyId;
}
