package com.elizaveta.service3.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FileInfoDTO {

    private String key;
    private long size;
    private Instant lastModified;
}