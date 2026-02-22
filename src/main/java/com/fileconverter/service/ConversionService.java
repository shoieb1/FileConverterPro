package com.fileconverter.service;

import com.fileconverter.model.ConversionResult;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.opencsv.CSVReader;

import javax.imageio.ImageIO;
// AWT – explicit imports to avoid POI Color/Font ambiguity
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ConversionService {
    private static final Logger log = LoggerFactory.getLogger(ConversionService.class);
    private static final Path TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"), "file-converter");

    @Value("${app.libreoffice.path}")
    private String libreOfficePath;

    public ConversionService() {
        try {
            Files.createDirectories(TEMP_DIR);
        } catch (IOException e) {
            log.error("Failed to create temp directory", e);
        }
    }

    // ============================================================
    // PDF TOOLS
    // ============================================================

    public ConversionResult pdfToWord(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".docx";
            Path outPath = convertWithLibreOffice(inputPath, "docx", outName);
            return buildResult(true, "PDF converted to Word successfully!", outName, "PDF → Word", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult wordToPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".pdf";
            Path outPath = convertWithLibreOffice(inputPath, "pdf", outName);
            return buildResult(true, "Word converted to PDF successfully!", outName, "Word → PDF", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    /**
     * Uses LibreOffice headless mode for high-fidelity document conversion.
     * Preserves original formatting, fonts, tables, images, and layout.
     */
    private Path convertWithLibreOffice(Path inputPath, String format, String outName) throws IOException {
        // Create a unique temp dir for LibreOffice output
        Path tempOutDir = Files.createTempDirectory("libreoffice_conv_");
        try {
            // Detect if the input is a PDF — LibreOffice needs a special import filter
            String inputExt = FilenameUtils.getExtension(inputPath.getFileName().toString()).toLowerCase();
            List<String> command = new ArrayList<>();
            command.add(libreOfficePath);
            command.add("--headless");
            if ("pdf".equals(inputExt)) {
                command.add("--infilter=writer_pdf_import");
            }
            command.add("--convert-to");
            command.add(format);
            command.add("--outdir");
            command.add(tempOutDir.toString());
            command.add(inputPath.toString());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output to prevent blocking
            String processOutput = new String(process.getInputStream().readAllBytes());
            log.debug("LibreOffice output: {}", processOutput);

            boolean completed = process.waitFor(120, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new IOException("LibreOffice conversion timed out after 120 seconds");
            }
            if (process.exitValue() != 0) {
                throw new IOException(
                        "LibreOffice conversion failed (exit code " + process.exitValue() + "): " + processOutput);
            }

            // Find the converted output file
            String baseName = FilenameUtils.getBaseName(inputPath.getFileName().toString());
            Path convertedFile = tempOutDir.resolve(baseName + "." + format);
            if (!Files.exists(convertedFile)) {
                // Sometimes LibreOffice changes the extension case; search for it
                try (var stream = Files.list(tempOutDir)) {
                    convertedFile = stream
                            .filter(p -> p.getFileName().toString().toLowerCase().endsWith("." + format.toLowerCase()))
                            .findFirst()
                            .orElseThrow(() -> new IOException("LibreOffice conversion produced no output file"));
                }
            }

            // Move to final location
            Path outPath = TEMP_DIR.resolve(outName);
            Files.move(convertedFile, outPath, StandardCopyOption.REPLACE_EXISTING);
            return outPath;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Conversion was interrupted", e);
        } finally {
            // Clean up the temporary LibreOffice output directory
            try {
                Files.walk(tempOutDir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });
            } catch (IOException ignored) {
            }
        }
    }

    public ConversionResult pptToPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".pdf";
            Path outPath = convertWithLibreOffice(inputPath, "pdf", outName);
            return buildResult(true, "PowerPoint converted to PDF successfully!", outName, "PPT → PDF", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult mergePdfs(List<MultipartFile> files) throws IOException {
        long start = System.currentTimeMillis();
        PDFMergerUtility merger = new PDFMergerUtility();
        List<Path> temps = new ArrayList<>();
        try {
            for (MultipartFile f : files) {
                Path p = saveTempFile(f);
                temps.add(p);
                merger.addSource(p.toFile());
            }
            String outName = "merged_" + System.currentTimeMillis() + ".pdf";
            Path outPath = TEMP_DIR.resolve(outName);
            merger.setDestinationFileName(outPath.toString());
            merger.mergeDocuments(null);
            return buildResult(true, "PDFs merged successfully!", outName, "PDF Merge", outPath, start);
        } finally {
            temps.forEach(this::deleteSilently);
        }
    }

    public ConversionResult splitPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try (PDDocument doc = Loader.loadPDF(inputPath.toFile())) {
            Splitter splitter = new Splitter();
            List<PDDocument> pages = splitter.split(doc);

            // Zip all pages
            String zipName = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_split.zip";
            Path zipPath = TEMP_DIR.resolve(zipName);
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                for (int i = 0; i < pages.size(); i++) {
                    PDDocument page = pages.get(i);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    page.save(baos);
                    page.close();
                    zos.putNextEntry(new ZipEntry("page_" + (i + 1) + ".pdf"));
                    zos.write(baos.toByteArray());
                    zos.closeEntry();
                }
            }
            return buildResult(true, "PDF split into " + pages.size() + " pages!", zipName, "PDF Split", zipPath,
                    start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult compressPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try (PDDocument doc = Loader.loadPDF(inputPath.toFile())) {
            doc.setAllSecurityToBeRemoved(true);
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_compressed.pdf";
            Path outPath = TEMP_DIR.resolve(outName);
            doc.save(outPath.toFile());
            return buildResult(true, "PDF compressed successfully!", outName, "PDF Compress", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult pdfToImages(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try (PDDocument doc = Loader.loadPDF(inputPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(doc);
            String base = FilenameUtils.getBaseName(file.getOriginalFilename());
            String zipName = base + "_images.zip";
            Path zipPath = TEMP_DIR.resolve(zipName);

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                for (int i = 0; i < doc.getNumberOfPages(); i++) {
                    BufferedImage img = renderer.renderImageWithDPI(i, 150, ImageType.RGB);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "PNG", baos);
                    zos.putNextEntry(new ZipEntry(base + "_page_" + (i + 1) + ".png"));
                    zos.write(baos.toByteArray());
                    zos.closeEntry();
                }
            }
            return buildResult(true, "PDF converted to images successfully!", zipName, "PDF → Images", zipPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult imagesToPdf(List<MultipartFile> files) throws IOException {
        long start = System.currentTimeMillis();
        List<Path> temps = new ArrayList<>();
        try (PDDocument pdf = new PDDocument()) {
            for (MultipartFile f : files) {
                Path p = saveTempFile(f);
                temps.add(p);
                BufferedImage img = ImageIO.read(p.toFile());
                if (img == null)
                    continue;
                PDPage page = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
                pdf.addPage(page);
                PDImageXObject pdImg = PDImageXObject.createFromFile(p.toString(), pdf);
                try (PDPageContentStream cs = new PDPageContentStream(pdf, page)) {
                    cs.drawImage(pdImg, 0, 0, img.getWidth(), img.getHeight());
                }
            }
            String outName = "images_" + System.currentTimeMillis() + ".pdf";
            Path outPath = TEMP_DIR.resolve(outName);
            pdf.save(outPath.toFile());
            return buildResult(true, "Images converted to PDF!", outName, "Images → PDF", outPath, start);
        } finally {
            temps.forEach(this::deleteSilently);
        }
    }

    public ConversionResult pdfToText(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".txt";
            Path outPath = convertWithLibreOffice(inputPath, "txt:Text (encoded):UTF8", outName);
            return buildResult(true, "Text extracted from PDF successfully!", outName, "PDF → Text", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult textToPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".pdf";
            Path outPath = convertWithLibreOffice(inputPath, "pdf", outName);
            return buildResult(true, "Text converted to PDF successfully!", outName, "Text → PDF", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    // ============================================================
    // IMAGE TOOLS
    // ============================================================

    public ConversionResult convertImage(MultipartFile file, String targetFormat) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String outName = baseName + "_converted." + targetFormat.toLowerCase();
        Path outPath = TEMP_DIR.resolve(outName);

        BufferedImage img = ImageIO.read(inputPath.toFile());
        if (img == null)
            throw new IOException("Cannot read image file");

        if ("jpg".equalsIgnoreCase(targetFormat) || "jpeg".equalsIgnoreCase(targetFormat)) {
            BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.createGraphics().drawImage(img, 0, 0, java.awt.Color.WHITE, null);
            ImageIO.write(rgb, "JPEG", outPath.toFile());
        } else {
            ImageIO.write(img, targetFormat.toUpperCase(), outPath.toFile());
        }

        deleteSilently(inputPath);
        String fromExt = FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase();
        return buildResult(true, fromExt + " converted to " + targetFormat.toUpperCase() + "!", outName,
                fromExt + " → " + targetFormat.toUpperCase(), outPath, start);
    }

    public ConversionResult resizeImage(MultipartFile file, int width, int height) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + width + "x" + height + "." + ext;
        Path outPath = TEMP_DIR.resolve(outName);

        Thumbnails.of(inputPath.toFile())
                .size(width, height)
                .keepAspectRatio(false)
                .toFile(outPath.toFile());

        deleteSilently(inputPath);
        return buildResult(true, "Image resized to " + width + "x" + height + "!", outName, "Image Resize", outPath,
                start);
    }

    public ConversionResult compressImage(MultipartFile file, float quality) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_compressed." + ext;
        Path outPath = TEMP_DIR.resolve(outName);

        Thumbnails.of(inputPath.toFile())
                .scale(1.0)
                .outputQuality(quality)
                .toFile(outPath.toFile());

        deleteSilently(inputPath);
        return buildResult(true, "Image compressed successfully!", outName, "Image Compress", outPath, start);
    }

    public ConversionResult imageToGrayscale(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_grayscale." + ext;
        Path outPath = TEMP_DIR.resolve(outName);

        BufferedImage src = ImageIO.read(inputPath.toFile());
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        String format = ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") ? "JPEG" : ext.toUpperCase();
        ImageIO.write(gray, format, outPath.toFile());
        deleteSilently(inputPath);
        return buildResult(true, "Image converted to grayscale!", outName, "Image → Grayscale", outPath, start);
    }

    // ============================================================
    // SPREADSHEET TOOLS
    // ============================================================

    public ConversionResult csvToExcel(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".xlsx";
            Path outPath = convertWithLibreOffice(inputPath, "xlsx", outName);
            return buildResult(true, "CSV converted to Excel successfully!", outName, "CSV → Excel", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult excelToCsv(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".csv";
            Path outPath = convertWithLibreOffice(inputPath, "csv", outName);
            return buildResult(true, "Excel converted to CSV successfully!", outName, "Excel → CSV", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult csvToJson(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);

        try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new FileReader(inputPath.toFile()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty())
                throw new IOException("Empty CSV file");

            String[] headers = rows.get(0);
            StringBuilder json = new StringBuilder("[\n");
            for (int i = 1; i < rows.size(); i++) {
                json.append("  {");
                String[] row = rows.get(i);
                for (int j = 0; j < headers.length; j++) {
                    String val = j < row.length ? row[j].replace("\"", "\\\"") : "";
                    json.append("\"").append(headers[j]).append("\": \"").append(val).append("\"");
                    if (j < headers.length - 1)
                        json.append(", ");
                }
                json.append("}");
                if (i < rows.size() - 1)
                    json.append(",");
                json.append("\n");
            }
            json.append("]");

            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".json";
            Path outPath = TEMP_DIR.resolve(outName);
            Files.writeString(outPath, json.toString());
            deleteSilently(inputPath);
            return buildResult(true, "CSV converted to JSON!", outName, "CSV → JSON", outPath, start);
        } catch (com.opencsv.exceptions.CsvException e) {
            throw new IOException("Failed to parse CSV: " + e.getMessage(), e);
        }
    }

    public ConversionResult jsonToCsv(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        String content = Files.readString(inputPath);

        // Simple JSON array to CSV conversion
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        List<Map<String, Object>> data = mapper.readValue(content,
                new com.fasterxml.jackson.core.type.TypeReference<>() {
                });

        if (data.isEmpty())
            throw new IOException("Empty JSON array");

        Set<String> keys = data.get(0).keySet();
        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", keys)).append("\n");

        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (String key : keys) {
                Object val = row.getOrDefault(key, "");
                values.add("\"" + String.valueOf(val).replace("\"", "\"\"") + "\"");
            }
            csv.append(String.join(",", values)).append("\n");
        }

        String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".csv";
        Path outPath = TEMP_DIR.resolve(outName);
        Files.writeString(outPath, csv.toString());
        deleteSilently(inputPath);
        return buildResult(true, "JSON converted to CSV!", outName, "JSON → CSV", outPath, start);
    }

    public ConversionResult excelToPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".pdf";
            Path outPath = convertWithLibreOffice(inputPath, "pdf", outName);
            return buildResult(true, "Excel converted to PDF successfully!", outName, "Excel → PDF", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult markdownToHtml(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        String md = Files.readString(inputPath);

        // Simple markdown conversion
        String html = md
                .replaceAll("(?m)^# (.+)$", "<h1>$1</h1>")
                .replaceAll("(?m)^## (.+)$", "<h2>$1</h2>")
                .replaceAll("(?m)^### (.+)$", "<h3>$1</h3>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.+?)\\*", "<em>$1</em>")
                .replaceAll("`(.+?)`", "<code>$1</code>")
                .replaceAll("(?m)^- (.+)$", "<li>$1</li>")
                .replaceAll("\n\n", "</p><p>")
                .replaceAll("(?m)^(?!<[h|l|p])(.+)$", "<p>$1</p>");

        String fullHtml = """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><title>Converted</title>
                <style>body{font-family:Arial,sans-serif;max-width:800px;margin:40px auto;padding:20px;line-height:1.6}
                h1,h2,h3{color:#333}code{background:#f4f4f4;padding:2px 6px;border-radius:3px}</style>
                </head><body>""" + html + "</body></html>";

        String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".html";
        Path outPath = TEMP_DIR.resolve(outName);
        Files.writeString(outPath, fullHtml);
        deleteSilently(inputPath);
        return buildResult(true, "Markdown converted to HTML!", outName, "Markdown → HTML", outPath, start);
    }

    public ConversionResult htmlToPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".pdf";
            Path outPath = convertWithLibreOffice(inputPath, "pdf", outName);
            return buildResult(true, "HTML converted to PDF successfully!", outName, "HTML → PDF", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    public ConversionResult rtfToPdf(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();
        Path inputPath = saveTempFile(file);
        try {
            String outName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".pdf";
            Path outPath = convertWithLibreOffice(inputPath, "pdf", outName);
            return buildResult(true, "RTF converted to PDF successfully!", outName, "RTF → PDF", outPath, start);
        } finally {
            deleteSilently(inputPath);
        }
    }

    // ============================================================
    // MEDIA TOOLS (requires FFmpeg)
    // ============================================================

    public ConversionResult convertMedia(MultipartFile file, String targetFormat) throws IOException {
        long start = System.currentTimeMillis();
        // Check if FFmpeg is available
        if (!isFfmpegAvailable()) {
            return ConversionResult.builder()
                    .success(false)
                    .message(
                            "FFmpeg is not installed. Please install FFmpeg to use media conversion tools. Visit https://ffmpeg.org/download.html")
                    .conversionType("Media Conversion")
                    .processingTimeMs(System.currentTimeMillis() - start)
                    .build();
        }

        Path inputPath = saveTempFile(file);
        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String outName = baseName + "." + targetFormat.toLowerCase();
        Path outPath = TEMP_DIR.resolve(outName);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", inputPath.toString(),
                    "-q:a", "2",
                    outPath.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(120, java.util.concurrent.TimeUnit.SECONDS);

            if (!finished || process.exitValue() != 0) {
                throw new IOException("FFmpeg conversion failed");
            }

            deleteSilently(inputPath);
            String from = FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase();
            return buildResult(true, from + " converted to " + targetFormat.toUpperCase() + "!", outName,
                    from + " → " + targetFormat.toUpperCase(), outPath, start);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Conversion interrupted");
        } finally {
            deleteSilently(inputPath);
        }
    }

    private boolean isFfmpegAvailable() {
        try {
            Process p = new ProcessBuilder("ffmpeg", "-version").start();
            return p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    public byte[] getFileBytes(String fileName) throws IOException {
        Path path = TEMP_DIR.resolve(fileName);
        if (!path.startsWith(TEMP_DIR))
            throw new SecurityException("Invalid path");
        return Files.readAllBytes(path);
    }

    public void deleteFile(String fileName) {
        try {
            Path path = TEMP_DIR.resolve(fileName);
            if (path.startsWith(TEMP_DIR))
                Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", fileName);
        }
    }

    private Path saveTempFile(MultipartFile file) throws IOException {
        String name = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = TEMP_DIR.resolve(name);
        file.transferTo(path.toFile());
        return path;
    }

    private ConversionResult buildResult(boolean success, String message, String fileName, String convType,
            Path outPath, long start) {
        return ConversionResult.builder()
                .success(success)
                .message(message)
                .fileName(fileName)
                .downloadUrl("/download/" + fileName)
                .conversionType(convType)
                .fileSizeBytes(outPath.toFile().length())
                .processingTimeMs(System.currentTimeMillis() - start)
                .build();
    }

    private void deleteSilently(Path path) {
        try {
            if (path != null)
                Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private byte[] imageToBytes(BufferedImage img, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, format, baos);
        return baos.toByteArray();
    }
}
