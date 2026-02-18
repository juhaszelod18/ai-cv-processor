package com.intuitech.intuitech.service;

import com.intuitech.intuitech.exception.DocumentParsingException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.apache.tika.exception.TikaException;

import java.io.IOException;

@Service
public class DocumentParserService {

    private final Tika tika;

    public DocumentParserService(Tika tika) {
        this.tika = tika;
    }

    public String parse(MultipartFile file) {
        try {
            return tika.parseToString(file.getInputStream());
        } catch (IOException | TikaException e) {
            throw new DocumentParsingException("Failed to parse uploaded document: " + file.getOriginalFilename(), e);
        }
    }
}
