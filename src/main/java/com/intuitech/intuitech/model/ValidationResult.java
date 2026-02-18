package com.intuitech.intuitech.model;

public record ValidationResult(
        String field,
        boolean passed,
        String reason
) {}
