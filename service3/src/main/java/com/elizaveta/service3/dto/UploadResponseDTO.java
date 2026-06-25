package com.elizaveta.service3.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponseDTO {

    private String key;
    private String bucket;
    private String message;
}