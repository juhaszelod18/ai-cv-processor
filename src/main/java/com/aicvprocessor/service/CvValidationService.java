package com.aicvprocessor.service;

import com.aicvprocessor.model.CvData;
import com.aicvprocessor.model.ValidationResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CvValidationService {

    private static final String PROFILE_VALIDATION_PROMPT = """
            You are evaluating a candidate's profile statement from their CV.
            
            Determine whether the profile expresses BOTH of the following aspirations. Wording may vary — judge the intent, not the exact phrasing:
            1. Interest in GenAI (or AI, machine learning, generative models, LLMs) and a desire to grow or become skilled in it.
            2. Interest in Java (or backend/software development) and a desire to grow or become skilled in it.

            Be lenient with phrasing — "eager to contribute", "keen interest", "passionate about" all count as expressing desire to grow.
            Both must be present for the validation to pass.
            Provide a brief reason for your decision.
            
            Profile:
            %s
            """;

    private final ChatClient chatClient;

    public CvValidationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<ValidationResult> validate(CvData cvData) {
        return List.of(
                validateWorkExperience(cvData.workExperienceYears()),
                validateSkills(cvData.skills()),
                validateLanguages(cvData.languages()),
                validateProfile(cvData.profile())
        );
    }

    private ValidationResult validateWorkExperience(double years) {
        boolean passed = years >= 0 && years <= 2;
        String reason = passed
                ? "%.1f years is within the 0–2 year range".formatted(years)
                : "%.1f years is outside the required 0–2 year range".formatted(years);
        return new ValidationResult("workExperience", passed, reason);
    }

    private ValidationResult validateSkills(List<String> skills) {
        List<String> missing = new ArrayList<>();
        if (doesNotContainIgnoreCase(skills, "java")) missing.add("Java");
        if (skills.stream().noneMatch(s -> s.toLowerCase().contains("llm"))) missing.add("LLMs");

        if (missing.isEmpty()) {
            return new ValidationResult("skills", true, "Skills include both Java and LLMs");
        }
        return new ValidationResult("skills", false, "Missing required skills: " + String.join(", ", missing));
    }

    private ValidationResult validateLanguages(List<String> languages) {
        List<String> missing = new ArrayList<>();
        if (doesNotContainIgnoreCase(languages, "hungarian")) missing.add("Hungarian");
        if (doesNotContainIgnoreCase(languages, "english")) missing.add("English");

        if (missing.isEmpty()) {
            return new ValidationResult("languages", true, "Languages include both Hungarian and English");
        }
        return new ValidationResult("languages", false, "Missing required languages: " + String.join(", ", missing));
    }

    private ValidationResult validateProfile(String profile) {
        ProfileValidation result = chatClient.prompt()
                .user(PROFILE_VALIDATION_PROMPT.formatted(profile))
                .call()
                .entity(ProfileValidation.class);
        if (result == null) {
            throw new IllegalStateException("Profile validation returned no result from LLM");
        }
        return new ValidationResult("profile", result.passed(), result.reason());
    }

    private boolean doesNotContainIgnoreCase(List<String> list, String target) {
        return list.stream().noneMatch(s -> s.equalsIgnoreCase(target));
    }

    private record ProfileValidation(boolean passed, String reason) {
    }
}
