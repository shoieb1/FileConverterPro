package com.fileconverter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolInfo {
    private String id;
    private String name;
    private String description;
    private String icon;
    private String category;
    private String fromFormat;
    private String toFormat;
    private String acceptedFormats;
    private String color;
    private boolean popular;
}
