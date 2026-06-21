package com.elizaveta.service1.mapper;

import com.elizaveta.service1.dto.ConversionRequestDTO;
import com.elizaveta.service1.entity.ConversionRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversionRequestMapper {

    ConversionRequestDTO toDto(ConversionRequest entity);
}