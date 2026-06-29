package com.elizaveta.service3.handler;

import com.elizaveta.service3.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Ошибка запроса: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        ));
    }

    @ExceptionHandler(NoSuchKeyException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoSuchKey(
            NoSuchKeyException ex, WebRequest request) {

        log.warn("Файл не найден: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        ));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            NoHandlerFoundException ex, WebRequest request) {

        log.warn("Запрошен несуществующий URL: {}", ex.getRequestURL());

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "URL не найден: " + ex.getRequestURL(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponseDTO> handleMultipartException(
            MultipartException ex, WebRequest request) {

        log.warn("Ошибка загрузки файла: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Файл не прикреплён или запрос не является multipart",
                request.getDescription(false).replace("uri=", "")
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        log.warn("Неподдерживаемый метод запроса: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                "Метод " + ex.getMethod() + " не поддерживается для данного URL",
                request.getDescription(false).replace("uri=", "")
        ));
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleS3Exception(
            S3Exception ex, WebRequest request) {

        log.warn("Ошибка S3: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.awsErrorDetails().errorMessage(),
                request.getDescription(false).replace("uri=", "")
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(
            Exception ex, WebRequest request) {

        log.error("Внутренняя ошибка", ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Произошла внутренняя ошибка сервера",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.internalServerError().body(error);
    }
}