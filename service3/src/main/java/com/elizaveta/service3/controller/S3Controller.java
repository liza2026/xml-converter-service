package com.elizaveta.service3.controller;

import com.elizaveta.service3.dto.FileInfoDTO;
import com.elizaveta.service3.dto.UploadResponseDTO;
import com.elizaveta.service3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST-контроллер для управления файлами в S3-совместимом хранилище (MinIO).
 * <p>
 * Предоставляет REST-эндпоинты для загрузки, скачивания, получения списка
 * и удаления файлов. Все операции делегируются {@link S3Service}.
 * Базовый путь ко всем эндпоинтам: {@code /s3}.
 */
@Slf4j
@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    /**
     * Загружает файл в S3-хранилище.
     * <p>
     * Принимает multipart-запрос с файлом, передаёт его в сервис для загрузки
     * и возвращает информацию о загруженном файле.
     *
     * @param file загружаемый файл, переданный в теле запроса с параметром {@code file}
     * @return {@link ResponseEntity} с DTO {@link UploadResponseDTO}
     * в теле ответа и статусом 200 OK
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> upload(@RequestParam("file") MultipartFile file) {
        log.info("Получен запрос на загрузку файла: {}", file.getOriginalFilename());
        UploadResponseDTO response = s3Service.upload(file);
        log.info("Загрузка файла завершена: {}", file.getOriginalFilename());
        return ResponseEntity.ok(response);
    }

    /**
     * Скачивает файл из S3-хранилища по его ключу.
     * <p>
     * Получает файл из сервиса в виде массива байтов и возвращает его клиенту
     * как вложение с указанием имени файла в заголовке
     * {@code Content-Disposition}.
     *
     * @param key ключ (имя) файла в бакете, переданный в пути запроса
     * @return {@link ResponseEntity} с массивом байтов файла в теле ответа,
     * типом содержимого {@code application/octet-stream},
     * заголовком для скачивания и статусом 200 OK
     */
    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) {
        log.info("Получен запрос на скачивание файла: {}", key);
        byte[] bytes = s3Service.download(key);
        log.info("Скачивание файла завершено: {}", key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    /**
     * Возвращает список всех файлов в бакете.
     * <p>
     * Делегирует запрос в сервис и возвращает список файлов с их
     * метаданными (ключ, размер, дата последнего изменения).
     *
     * @return {@link ResponseEntity} со списком {@link FileInfoDTO}
     * в теле ответа и статусом 200 OK
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileInfoDTO>> list() {
        log.info("Получен запрос на получение списка файлов");
        List<FileInfoDTO> files = s3Service.list();
        log.info("Получено {} файлов", files.size());
        return ResponseEntity.ok(files);
    }

    /**
     * Удаляет файл из S3-хранилища по его ключу (имени).
     * <p>
     * Делегирует удаление в сервис и возвращает пустой ответ с кодом 204 No Content
     * при успешном удалении.
     *
     * @param key ключ (имя) файла в бакете, переданный в пути запроса
     * @return {@link ResponseEntity} с пустым телом и статусом 204 No Content
     */
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        log.info("Получен запрос на удаление файла: {}", key);
        s3Service.delete(key);
        log.info("Удаление файла завершено: {}", key);
        return ResponseEntity.noContent().build();
    }
}