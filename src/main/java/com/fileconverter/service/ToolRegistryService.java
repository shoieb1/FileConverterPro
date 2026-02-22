package com.fileconverter.service;

import com.fileconverter.model.ToolInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolRegistryService {

    private final List<ToolInfo> tools = new ArrayList<>();

    public ToolRegistryService() {
        // PDF Tools
        tools.add(ToolInfo.builder().id("pdf-to-word").name("PDF → Word").description("Convert PDF documents to editable Word files").icon("📄").category("PDF").fromFormat("PDF").toFormat("DOCX").acceptedFormats(".pdf").color("#e74c3c").popular(true).build());
        tools.add(ToolInfo.builder().id("word-to-pdf").name("Word → PDF").description("Convert Word documents to PDF format").icon("📝").category("PDF").fromFormat("DOCX").toFormat("PDF").acceptedFormats(".doc,.docx").color("#e74c3c").popular(true).build());
        tools.add(ToolInfo.builder().id("ppt-to-pdf").name("PPT → PDF").description("Convert PowerPoint presentations to PDF").icon("📊").category("PDF").fromFormat("PPTX").toFormat("PDF").acceptedFormats(".ppt,.pptx").color("#e74c3c").popular(true).build());
        tools.add(ToolInfo.builder().id("pdf-to-ppt").name("PDF → PPT").description("Convert PDF slides to PowerPoint").icon("🖼️").category("PDF").fromFormat("PDF").toFormat("PPTX").acceptedFormats(".pdf").color("#e74c3c").popular(false).build());
        tools.add(ToolInfo.builder().id("pdf-merge").name("Merge PDFs").description("Combine multiple PDF files into one").icon("🔗").category("PDF").fromFormat("PDF").toFormat("PDF").acceptedFormats(".pdf").color("#e74c3c").popular(true).build());
        tools.add(ToolInfo.builder().id("pdf-split").name("Split PDF").description("Split a PDF into separate pages").icon("✂️").category("PDF").fromFormat("PDF").toFormat("PDF").acceptedFormats(".pdf").color("#e74c3c").popular(false).build());
        tools.add(ToolInfo.builder().id("pdf-compress").name("Compress PDF").description("Reduce PDF file size").icon("🗜️").category("PDF").fromFormat("PDF").toFormat("PDF").acceptedFormats(".pdf").color("#e74c3c").popular(true).build());
        tools.add(ToolInfo.builder().id("pdf-to-images").name("PDF → Images").description("Convert PDF pages to PNG images").icon("🖼️").category("PDF").fromFormat("PDF").toFormat("PNG").acceptedFormats(".pdf").color("#e74c3c").popular(false).build());
        tools.add(ToolInfo.builder().id("images-to-pdf").name("Images → PDF").description("Convert images to a single PDF").icon("📦").category("PDF").fromFormat("Images").toFormat("PDF").acceptedFormats(".jpg,.jpeg,.png,.gif,.bmp,.webp").color("#e74c3c").popular(false).build());

        // Image Tools
        tools.add(ToolInfo.builder().id("jpg-to-png").name("JPG → PNG").description("Convert JPEG images to PNG format").icon("🎨").category("Image").fromFormat("JPG").toFormat("PNG").acceptedFormats(".jpg,.jpeg").color("#3498db").popular(true).build());
        tools.add(ToolInfo.builder().id("png-to-jpg").name("PNG → JPG").description("Convert PNG images to JPEG format").icon("🖼️").category("Image").fromFormat("PNG").toFormat("JPG").acceptedFormats(".png").color("#3498db").popular(true).build());
        tools.add(ToolInfo.builder().id("webp-to-png").name("WebP → PNG").description("Convert WebP images to PNG format").icon("🌐").category("Image").fromFormat("WEBP").toFormat("PNG").acceptedFormats(".webp").color("#3498db").popular(false).build());
        tools.add(ToolInfo.builder().id("png-to-webp").name("PNG → WebP").description("Convert PNG images to WebP format").icon("⚡").category("Image").fromFormat("PNG").toFormat("WEBP").acceptedFormats(".png").color("#3498db").popular(false).build());
        tools.add(ToolInfo.builder().id("jpg-to-webp").name("JPG → WebP").description("Convert JPEG images to WebP format").icon("🚀").category("Image").fromFormat("JPG").toFormat("WEBP").acceptedFormats(".jpg,.jpeg").color("#3498db").popular(false).build());
        tools.add(ToolInfo.builder().id("image-resize").name("Resize Image").description("Resize images to custom dimensions").icon("↔️").category("Image").fromFormat("Image").toFormat("Image").acceptedFormats(".jpg,.jpeg,.png,.gif,.bmp,.webp").color("#3498db").popular(true).build());
        tools.add(ToolInfo.builder().id("image-compress").name("Compress Image").description("Reduce image file size without quality loss").icon("🗜️").category("Image").fromFormat("Image").toFormat("Image").acceptedFormats(".jpg,.jpeg,.png,.gif,.bmp").color("#3498db").popular(true).build());
        tools.add(ToolInfo.builder().id("image-to-grayscale").name("Image → Grayscale").description("Convert colorful images to grayscale").icon("⚫").category("Image").fromFormat("Image").toFormat("Image").acceptedFormats(".jpg,.jpeg,.png,.bmp").color("#3498db").popular(false).build());
        tools.add(ToolInfo.builder().id("gif-to-png").name("GIF → PNG").description("Convert GIF files to PNG format").icon("🎞️").category("Image").fromFormat("GIF").toFormat("PNG").acceptedFormats(".gif").color("#3498db").popular(false).build());
        tools.add(ToolInfo.builder().id("bmp-to-png").name("BMP → PNG").description("Convert BMP images to PNG format").icon("🖼️").category("Image").fromFormat("BMP").toFormat("PNG").acceptedFormats(".bmp").color("#3498db").popular(false).build());

        // Spreadsheet Tools
        tools.add(ToolInfo.builder().id("csv-to-excel").name("CSV → Excel").description("Convert CSV files to Excel spreadsheets").icon("📊").category("Spreadsheet").fromFormat("CSV").toFormat("XLSX").acceptedFormats(".csv").color("#27ae60").popular(true).build());
        tools.add(ToolInfo.builder().id("excel-to-csv").name("Excel → CSV").description("Convert Excel spreadsheets to CSV format").icon("📋").category("Spreadsheet").fromFormat("XLSX").toFormat("CSV").acceptedFormats(".xlsx,.xls").color("#27ae60").popular(true).build());
        tools.add(ToolInfo.builder().id("csv-to-json").name("CSV → JSON").description("Convert CSV data to JSON format").icon("{}").category("Spreadsheet").fromFormat("CSV").toFormat("JSON").acceptedFormats(".csv").color("#27ae60").popular(true).build());
        tools.add(ToolInfo.builder().id("json-to-csv").name("JSON → CSV").description("Convert JSON data to CSV format").icon("📄").category("Spreadsheet").fromFormat("JSON").toFormat("CSV").acceptedFormats(".json").color("#27ae60").popular(false).build());
        tools.add(ToolInfo.builder().id("excel-to-pdf").name("Excel → PDF").description("Convert Excel spreadsheets to PDF").icon("📃").category("Spreadsheet").fromFormat("XLSX").toFormat("PDF").acceptedFormats(".xlsx,.xls").color("#27ae60").popular(false).build());

        // Document Tools
        tools.add(ToolInfo.builder().id("txt-to-pdf").name("Text → PDF").description("Convert plain text files to PDF").icon("📝").category("Document").fromFormat("TXT").toFormat("PDF").acceptedFormats(".txt").color("#9b59b6").popular(true).build());
        tools.add(ToolInfo.builder().id("pdf-to-txt").name("PDF → Text").description("Extract text from PDF documents").icon("📝").category("Document").fromFormat("PDF").toFormat("TXT").acceptedFormats(".pdf").color("#9b59b6").popular(true).build());
        tools.add(ToolInfo.builder().id("html-to-pdf").name("HTML → PDF").description("Convert HTML files to PDF documents").icon("🌐").category("Document").fromFormat("HTML").toFormat("PDF").acceptedFormats(".html,.htm").color("#9b59b6").popular(false).build());
        tools.add(ToolInfo.builder().id("md-to-html").name("Markdown → HTML").description("Convert Markdown to HTML").icon("📖").category("Document").fromFormat("MD").toFormat("HTML").acceptedFormats(".md,.markdown").color("#9b59b6").popular(false).build());
        tools.add(ToolInfo.builder().id("rtf-to-pdf").name("RTF → PDF").description("Convert RTF files to PDF format").icon("📄").category("Document").fromFormat("RTF").toFormat("PDF").acceptedFormats(".rtf").color("#9b59b6").popular(false).build());

        // Audio/Video Tools
        tools.add(ToolInfo.builder().id("video-to-audio").name("Video → Audio").description("Extract audio from video files (requires FFmpeg)").icon("🎵").category("Media").fromFormat("Video").toFormat("MP3").acceptedFormats(".mp4,.avi,.mov,.mkv,.webm,.flv").color("#f39c12").popular(true).build());
        tools.add(ToolInfo.builder().id("mp4-to-mp3").name("MP4 → MP3").description("Extract MP3 audio from MP4 video (requires FFmpeg)").icon("🎶").category("Media").fromFormat("MP4").toFormat("MP3").acceptedFormats(".mp4").color("#f39c12").popular(true).build());
        tools.add(ToolInfo.builder().id("mp3-to-wav").name("MP3 → WAV").description("Convert MP3 audio to WAV format (requires FFmpeg)").icon("🔊").category("Media").fromFormat("MP3").toFormat("WAV").acceptedFormats(".mp3").color("#f39c12").popular(false).build());
        tools.add(ToolInfo.builder().id("wav-to-mp3").name("WAV → MP3").description("Convert WAV audio to MP3 format (requires FFmpeg)").icon("🎸").category("Media").fromFormat("WAV").toFormat("MP3").acceptedFormats(".wav").color("#f39c12").popular(false).build());
    }

    public List<ToolInfo> getAllTools() {
        return tools;
    }

    public ToolInfo getToolById(String id) {
        return tools.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<ToolInfo> getPopularTools() {
        return tools.stream().filter(ToolInfo::isPopular).toList();
    }

    public Map<String, List<ToolInfo>> getToolsByCategory() {
        Map<String, List<ToolInfo>> map = new LinkedHashMap<>();
        for (ToolInfo t : tools) {
            map.computeIfAbsent(t.getCategory(), k -> new ArrayList<>()).add(t);
        }
        return map;
    }
}
