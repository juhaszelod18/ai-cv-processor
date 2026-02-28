package com.aicvprocessor.service;

import com.aicvprocessor.model.CvData;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CvExtractionService {

    private static final String EXTRACTION_PROMPT = """
            You are a precise CV data extractor. Extract the following fields from the CV text below.

            Rules:
            - workExperienceYears: calculate the total years of professional work experience \
            across all jobs. Return 0 if none found. Use decimal values if needed (e.g. 1.5 for 18 months).
            - skills: list all technical and professional skills mentioned. If the CV references \
            any LLM-related skills (e.g. Generative AI, prompt engineering, OpenAI, GPT, ChatGPT, Gemini AI), \
            include "LLMs" as a skill entry.
            - languages: list all spoken/written languages mentioned. Include only the language name \
            (e.g. "English", "Hungarian") — do not include proficiency levels or qualifiers.
            - profile: extract the profile summary or personal statement as-is. \
            If none exists, summarize the candidate's background in 2-3 sentences.

            Return only valid JSON. No markdown, no explanation, no extra text.

            CV Text:
            %s
            """;

    private final ChatClient chatClient;

    public CvExtractionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public CvData extract(String documentText) {
        return chatClient.prompt()
                .user(EXTRACTION_PROMPT.formatted(documentText))
                .call()
                .entity(CvData.class);
    }
}
