package com.elizaveta.service1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversion_requests")
@Getter
@Setter
@NoArgsConstructor
public class ConversionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "json_result", columnDefinition = "TEXT", nullable = false)
    private String jsonResult;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "processing_time_ms", nullable = false)
    private Long processingTimeMs;

    @Column(name = "xml_tags_count", nullable = false)
    private Integer xmlTagsCount;

    @Column(name = "json_keys_count", nullable = false)
    private Integer jsonKeysCount;

    public ConversionRequest(String jsonResult, LocalDateTime requestDate,
                             Long processingTimeMs, Integer xmlTagsCount,
                             Integer jsonKeysCount) {

        this.jsonResult = jsonResult;
        this.requestDate = requestDate;
        this.processingTimeMs = processingTimeMs;
        this.xmlTagsCount = xmlTagsCount;
        this.jsonKeysCount = jsonKeysCount;
    }
}