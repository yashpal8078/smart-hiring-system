package com.smarthiring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDto {

    private String to;
    private String toName;
    private String subject;
    private String body;
    private String templateName;
    private Map<String, Object> templateVariables;
    private boolean isHtml;
}