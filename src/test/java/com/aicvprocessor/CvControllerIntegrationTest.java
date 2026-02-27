package com.aicvprocessor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CvControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validCv_shouldPassAllValidations() throws Exception {
        byte[] cv = Files.readAllBytes(Path.of("src/test/resources/CV-2.pdf"));
        MockMultipartFile file = new MockMultipartFile("file", "CV-2.pdf", "application/pdf", cv);

        mockMvc.perform(multipart("/api/cv/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validations[?(@.field == 'workExperience')].passed").value(true))
                .andExpect(jsonPath("$.validations[?(@.field == 'skills')].passed").value(true))
                .andExpect(jsonPath("$.validations[?(@.field == 'languages')].passed").value(true))
                .andExpect(jsonPath("$.validations[?(@.field == 'profile')].passed").value(true));
    }

    @Test
    void invalidCv_shouldFailSkillsAndProfile() throws Exception {
        byte[] cv = Files.readAllBytes(Path.of("src/test/resources/CV-1.pdf"));
        MockMultipartFile file = new MockMultipartFile("file", "CV-1.pdf", "application/pdf", cv);

        mockMvc.perform(multipart("/api/cv/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validations[?(@.field == 'workExperience')].passed").value(true))
                .andExpect(jsonPath("$.validations[?(@.field == 'skills')].passed").value(false))
                .andExpect(jsonPath("$.validations[?(@.field == 'languages')].passed").value(true))
                .andExpect(jsonPath("$.validations[?(@.field == 'profile')].passed").value(false));
    }
}