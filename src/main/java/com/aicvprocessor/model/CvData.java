package com.aicvprocessor.model;

import java.util.List;

public record CvData(
        double workExperienceYears,
        List<String> skills,
        List<String> languages,
        String profile
) {}
