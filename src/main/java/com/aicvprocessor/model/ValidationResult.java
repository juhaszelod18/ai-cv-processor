package com.aicvprocessor.model;

public record ValidationResult(
        String field,
        boolean passed,
        String reason
) {}
