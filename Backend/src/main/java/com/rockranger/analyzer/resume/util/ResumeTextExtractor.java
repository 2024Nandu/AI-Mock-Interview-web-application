package com.rockranger.analyzer.resume.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class ResumeTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ResumeTextExtractor.class);

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase();

        try {
            switch (extension) {
                case "pdf":
                    return extractFromPdf(file);
                case "docx":
                    return extractFromDocx(file);
                case "txt":
                    return new String(file.getBytes(), StandardCharsets.UTF_8).trim();
                default:
                    logger.warn("Unsupported file extension for text extraction: {}", extension);
                    return "";
            }
        } catch (Exception e) {
            logger.error("Failed to extract text from file: {}", originalFilename, e);
            return "";
        }
    }

    private String extractFromPdf(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            logger.error("Error reading PDF content", e);
            return "";
        }
    }

    private String extractFromDocx(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            logger.error("Error reading DOCX content", e);
            return "";
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
