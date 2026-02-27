package com.aicvprocessor.model;

import java.util.List;

public record CvAnalysisResponse(
        CvData extracted,
        List<ValidationResult> validations
) {}
