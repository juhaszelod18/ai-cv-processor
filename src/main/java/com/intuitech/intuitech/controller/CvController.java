package com.intuitech.intuitech.controller;

import com.intuitech.intuitech.exception.DocumentParsingException;
import com.intuitech.intuitech.model.CvAnalysisResponse;
import com.intuitech.intuitech.model.CvData;
import com.intuitech.intuitech.service.CvExtractionService;
import com.intuitech.intuitech.service.CvValidationService;
import com.intuitech.intuitech.service.DocumentParserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final DocumentParserService documentParserService;
    private final CvExtractionService cvExtractionService;
    private final CvValidationService cvValidationService;

    public CvController(DocumentParserService documentParserService,
                        CvExtractionService cvExtractionService,
                        CvValidationService cvValidationService) {
        this.documentParserService = documentParserService;
        this.cvExtractionService = cvExtractionService;
        this.cvValidationService = cvValidationService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvAnalysisResponse> analyze(@RequestParam MultipartFile file) {
        String text = documentParserService.parse(file);
        CvData extracted = cvExtractionService.extract(text);
        return ResponseEntity.ok(new CvAnalysisResponse(extracted, cvValidationService.validate(extracted)));
    }

    @ExceptionHandler(DocumentParsingException.class)
    public ResponseEntity<String> handleDocumentParsingException(DocumentParsingException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
    }
}