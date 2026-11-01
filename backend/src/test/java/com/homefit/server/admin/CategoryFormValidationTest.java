package com.homefit.server.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class CategoryFormValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static CategoryForm form(String name, String slug, String iconUrl, Integer sortOrder) {
        CategoryForm f = new CategoryForm();
        f.setName(name);
        f.setSlug(slug);
        f.setIconUrl(iconUrl);
        f.setSortOrder(sortOrder);
        return f;
    }

    @Test
    void validFormHasNoViolations() {
        assertTrue(VALIDATOR.validate(form("Bụng", "abs", "", 1)).isEmpty());
        assertTrue(VALIDATOR.validate(form("Toàn thân", "full_body", "https://example.com/i.png", 4)).isEmpty());
    }

    @Test
    void blankNameIsRejected() {
        assertEquals(1, VALIDATOR.validate(form("  ", "abs", "", 1)).size());
    }

    @Test
    void slugMustBeLowerCaseAscii() {
        assertEquals(1, VALIDATOR.validate(form("Bụng", "Abs", "", 1)).size());
        assertEquals(1, VALIDATOR.validate(form("Bụng", "bụng", "", 1)).size());
        assertEquals(1, VALIDATOR.validate(form("Bụng", "full body", "", 1)).size());
        assertEquals(1, VALIDATOR.validate(form("Bụng", "", "", 1)).size());
    }

    @Test
    void iconUrlMustBeHttpOrEmpty() {
        assertEquals(1, VALIDATOR.validate(form("Bụng", "abs", "ftp://example.com/i.png", 1)).size());
        assertEquals(1, VALIDATOR.validate(form("Bụng", "abs", "example.com/i.png", 1)).size());
    }

    @Test
    void sortOrderMustBeInRange() {
        assertEquals(1, VALIDATOR.validate(form("Bụng", "abs", "", -1)).size());
        assertEquals(1, VALIDATOR.validate(form("Bụng", "abs", "", 10000)).size());
        assertEquals(1, VALIDATOR.validate(form("Bụng", "abs", "", null)).size());
    }
}
