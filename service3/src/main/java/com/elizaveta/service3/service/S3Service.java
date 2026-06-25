package com.elizaveta.service3.service;

import com.elizaveta.service3.dto.FileInfoDTO;
import com.elizaveta.service3.dto.UploadResponseDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

/**
 * Сервис для работы с S3-совместимым хранилищем (MinIO).
 * Предоставляет методы для загрузки, скачивания, получения списка
 * и удаления файлов в указанном бакете.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("Бакет {} уже существует", bucketName);
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Бакет {} создан", bucketName);
        }
    }

    /**
     * Загружает файл в S3-хранилище.
     * <p>
     * Перед загрузкой проверяет существование бакета и при необходимости
     * создаёт его. Имя файла (ключ) определяется как оригинальное имя
     * загружаемого файла.
     *
     * @param file загружаемый файл в виде {@link MultipartFile}
     * @return DTO с информацией о загруженном файле (ключ, бакет, сообщение)
     * @throws RuntimeException если не удалось прочитать байты файла
     */
    public UploadResponseDTO upload(MultipartFile file) {

        String key = file.getOriginalFilename();
        log.debug("Загрузка файла: {} в бакет: {}", key, bucketName);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            log.info("Файл успешно загружен: {}", key);

            return UploadResponseDTO.builder()
                    .key(key)
                    .bucket(bucketName)
                    .message("Файл успешно загружен")
                    .build();

        } catch (IOException e) {
            log.error("Не удалось загрузить файл: {}", key, e);
            throw new RuntimeException("Не удалось загрузить файл: " + key, e);
        }
    }

    /**
     * Скачивает файл из S3-хранилища по его ключу.
     *
     * @param key ключ (имя) файла в бакете
     * @return массив байтов содержимого файла
     * @throws RuntimeException если не удалось прочитать файл
     */
    public byte[] download(String key) {

        log.debug("Скачивание файла: {} из бакета: {}", key, bucketName);

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            log.info("Файл успешно скачан: {}", key);
            return response.readAllBytes();
        } catch (IOException e) {
            log.error("Не удалось скачать файл: {}", key, e);
            throw new RuntimeException("Не удалось скачать файл: " + key, e);
        }
    }

    /**
     * Возвращает список всех файлов в бакете.
     * <p>
     * Перед получением списка проверяет существование бакета
     * и при необходимости создаёт его.
     *
     * @return список DTO с информацией о каждом файле (ключ, размер, дата изменения)
     */
    public List<FileInfoDTO> list() {

        log.debug("Получение списка файлов в бакете: {}", bucketName);

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<FileInfoDTO> files = response.contents().stream()
                .map(obj -> FileInfoDTO.builder()
                        .key(obj.key())
                        .size(obj.size())
                        .lastModified(obj.lastModified())
                        .build())
                .toList();

        log.info("Получено {} файлов в бакете: {}", files.size(), bucketName);
        return files;
    }

    /**
     * Удаляет файл из S3-хранилища по его ключу.
     *
     * @param key ключ (имя) файла в бакете
     */
    public void delete(String key) {

        log.debug("Удаление файла: {} из бакета: {}", key, bucketName);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(request);
        log.info("Файл успешно удалён: {}", key);
    }
}