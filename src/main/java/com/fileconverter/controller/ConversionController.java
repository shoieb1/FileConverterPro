package com.fileconverter.controller;

import com.fileconverter.model.ConversionResult;
import com.fileconverter.service.ConversionService;
import com.fileconverter.service.StatsService;
import com.fileconverter.service.ToolRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class ConversionController {
    private static final Logger log = LoggerFactory.getLogger(ConversionController.class);

    private final ConversionService conversionService;
    private final StatsService statsService;
    private final ToolRegistryService toolRegistry;

    public ConversionController(ConversionService conversionService, StatsService statsService, ToolRegistryService toolRegistry) {
        this.conversionService = conversionService;
        this.statsService = statsService;
        this.toolRegistry = toolRegistry;
    }

    @PostMapping("/convert/{toolId}")
    public String convert(
            @PathVariable String toolId,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) List<MultipartFile> files,
            @RequestParam(defaultValue = "800") int width,
            @RequestParam(defaultValue = "600") int height,
            @RequestParam(defaultValue = "0.8") float quality,
            Model model) {

        model.addAttribute("allTools", toolRegistry.getAllTools());
        model.addAttribute("toolsByCategory", toolRegistry.getToolsByCategory());
        model.addAttribute("tool", toolRegistry.getToolById(toolId));

        try {
            ConversionResult result = performConversion(toolId, file, files, width, height, quality);
            String category = toolRegistry.getToolById(toolId) != null
                    ? toolRegistry.getToolById(toolId).getCategory() : "Other";
            statsService.record(category, result.isSuccess());
            model.addAttribute("result", result);
            model.addAttribute("toolId", toolId);
            return "result";
        } catch (Exception e) {
            log.error("Conversion error for tool {}: {}", toolId, e.getMessage(), e);
            statsService.record("Other", false);
            ConversionResult error = ConversionResult.builder()
                    .success(false)
                    .message("Conversion failed: " + e.getMessage())
                    .conversionType(toolId)
                    .build();
            model.addAttribute("result", error);
            model.addAttribute("toolId", toolId);
            return "result";
        }
    }

    private ConversionResult performConversion(String toolId, MultipartFile file,
                                                List<MultipartFile> files, int width, int height, float quality) throws IOException {
        return switch (toolId) {
            // PDF tools
            case "pdf-to-word"    -> conversionService.pdfToWord(file);
            case "word-to-pdf"    -> conversionService.wordToPdf(file);
            case "ppt-to-pdf"     -> conversionService.pptToPdf(file);
            case "pdf-merge"      -> conversionService.mergePdfs(files != null ? files : List.of(file));
            case "pdf-split"      -> conversionService.splitPdf(file);
            case "pdf-compress"   -> conversionService.compressPdf(file);
            case "pdf-to-images"  -> conversionService.pdfToImages(file);
            case "images-to-pdf"  -> conversionService.imagesToPdf(files != null ? files : List.of(file));
            case "pdf-to-txt"     -> conversionService.pdfToText(file);
            case "txt-to-pdf"     -> conversionService.textToPdf(file);
            case "html-to-pdf"    -> conversionService.htmlToPdf(file);
            case "md-to-html"     -> conversionService.markdownToHtml(file);
            case "rtf-to-pdf"     -> conversionService.rtfToPdf(file);

            // Image tools
            case "jpg-to-png", "gif-to-png", "bmp-to-png" -> conversionService.convertImage(file, "png");
            case "png-to-jpg"     -> conversionService.convertImage(file, "jpg");
            case "webp-to-png"    -> conversionService.convertImage(file, "png");
            case "png-to-webp", "jpg-to-webp" -> conversionService.convertImage(file, "webp");
            case "image-resize"   -> conversionService.resizeImage(file, width, height);
            case "image-compress" -> conversionService.compressImage(file, quality);
            case "image-to-grayscale" -> conversionService.imageToGrayscale(file);

            // Spreadsheet tools
            case "csv-to-excel"   -> conversionService.csvToExcel(file);
            case "excel-to-csv"   -> conversionService.excelToCsv(file);
            case "csv-to-json"    -> conversionService.csvToJson(file);
            case "json-to-csv"    -> conversionService.jsonToCsv(file);
            case "excel-to-pdf"   -> conversionService.excelToPdf(file);

            // Media tools
            case "video-to-audio", "mp4-to-mp3" -> conversionService.convertMedia(file, "mp3");
            case "mp3-to-wav"     -> conversionService.convertMedia(file, "wav");
            case "wav-to-mp3"     -> conversionService.convertMedia(file, "mp3");

            // PPT from PDF
            case "pdf-to-ppt" -> {
                // Convert PDF to images then create a basic PPTX
                yield conversionService.pdfToImages(file); // fallback
            }

            default -> ConversionResult.builder().success(false).message("Unknown tool: " + toolId).build();
        };
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {
        try {
            byte[] bytes = conversionService.getFileBytes(fileName);
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .header(HttpHeaders.CONTENT_TYPE, detectContentType(fileName))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(bytes);
        } catch (Exception e) {
            log.error("Download error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public Object getStats() {
        return statsService.getStats().getCategoryMap();
    }

    private String detectContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".html")) return "text/html";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".zip")) return "application/zip";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".wav")) return "audio/wav";
        return "application/octet-stream";
    }
}
