package com.nadi.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DocumentParser {

    private static final Logger log = LoggerFactory.getLogger(DocumentParser.class);

    public ParsedDocument parse(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        String extension = getExtension(filename);

        return switch (extension.toLowerCase()) {
            case "pdf" -> parsePdf(file);
            case "csv" -> parseCsv(file);
            case "txt", "md" -> parseText(file);
            case "png", "jpg", "jpeg", "gif", "webp" -> parseImage(file, filename);
            default -> parseGeneric(file, filename, contentType);
        };
    }

    private ParsedDocument parsePdf(MultipartFile file) {
        try {
            String text = extractTextFromPdf(file);
            return new ParsedDocument("pdf", text, Map.of(
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "pageCount", estimatePageCount(text),
                "type", "document"
            ));
        } catch (Exception e) {
            log.error("PDF parse error: {}", e.getMessage());
            return new ParsedDocument("pdf", "", Map.of("error", e.getMessage()));
        }
    }

    private ParsedDocument parseCsv(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            int rowCount = Math.max(0, lines.length - 1);
            String[] headers = lines.length > 0 ? lines[0].split(",") : new String[0];

            return new ParsedDocument("csv", content, Map.of(
                "filename", file.getOriginalFilename(),
                "headers", headers,
                "rowCount", rowCount,
                "columnCount", headers.length,
                "type", "tabular"
            ));
        } catch (Exception e) {
            return new ParsedDocument("csv", "", Map.of("error", e.getMessage()));
        }
    }

    private ParsedDocument parseText(MultipartFile file) {
        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            return new ParsedDocument("text", text, Map.of(
                "filename", file.getOriginalFilename(),
                "size", file.getSize(),
                "charCount", text.length(),
                "type", "text"
            ));
        } catch (Exception e) {
            return new ParsedDocument("text", "", Map.of("error", e.getMessage()));
        }
    }

    private ParsedDocument parseImage(MultipartFile file, String filename) {
        try {
            return new ParsedDocument("image", "[Image: " + filename + "]", Map.of(
                "filename", filename,
                "size", file.getSize(),
                "contentType", file.getContentType(),
                "type", "image",
                "note", "L'analyse d'image nécessite un modèle vision (MIMO Omni ou GPT-4o)"
            ));
        } catch (Exception e) {
            return new ParsedDocument("image", "", Map.of("error", e.getMessage()));
        }
    }

    private ParsedDocument parseGeneric(MultipartFile file, String filename, String contentType) {
        return new ParsedDocument("unknown", "", Map.of(
            "filename", filename,
            "contentType", contentType,
            "size", file.getSize(),
            "type", "unsupported",
            "message", "Type de fichier non supporté pour l'analyse automatique"
        ));
    }

    private String extractTextFromPdf(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "[PDF content - extraction requires Apache Tika]";
        }
    }

    private int estimatePageCount(String text) {
        if (text == null || text.isEmpty()) return 0;
        int newlines = text.split("\n").length;
        return Math.max(1, newlines / 50);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "unknown";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public record ParsedDocument(String type, String content, Map<String, Object> metadata) {}
}
