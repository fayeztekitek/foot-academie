package com.nadi.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test: a <2 Mo photo inflates ~33% in base64 (~2.8M chars) and
 * used to be rejected by the 2M-char pattern (HTTP 400 with a silent UI).
 * The client now compresses, and the ceiling accepts ~3.5 Mo of binary.
 */
class JoueurRequestValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    private JoueurRequest base() {
        JoueurRequest request = new JoueurRequest();
        request.setPrenom("Amine");
        request.setNom("Zouari");
        request.setDateNaissance(LocalDate.of(2015, 10, 30));
        return request;
    }

    private String dataUrl(int chars) {
        return "data:image/jpeg;base64," + "A".repeat(chars);
    }

    @Test
    void acceptsCompressedPhonePhoto() {
        JoueurRequest request = base();
        request.setPhotoUrl(dataUrl(400_000)); // ~300 Ko JPEG after compression

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsTwoMbPhotoAfterBase64Inflation() {
        JoueurRequest request = base();
        request.setPhotoUrl(dataUrl(2_800_000)); // ~2 Mo file inflated

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsOversizedPayload() {
        JoueurRequest request = base();
        request.setPhotoUrl(dataUrl(6_000_000));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsJavascriptUrl() {
        JoueurRequest request = base();
        request.setPhotoUrl("javascript:alert(1)");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void nullPhotoIsValid() {
        assertTrue(validator.validate(base()).isEmpty());
    }
}
