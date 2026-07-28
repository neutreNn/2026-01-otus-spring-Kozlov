package ru.otus.homevault.storage.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.otus.homevault.storage.config.MinioProperties;
import ru.otus.homevault.storage.dto.StoredObject;
import ru.otus.homevault.storage.exception.StorageException;
import ru.otus.homevault.storage.exception.StorageObjectNotFoundException;

import java.io.InputStream;

@Service
@ConditionalOnProperty(prefix = "homevault.storage", name = "provider", havingValue = "minio", matchIfMissing = true)
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;

    private final MinioProperties properties;

    public MinioFileStorageService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @PostConstruct
    void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.bucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.bucket())
                        .build());
            }
        } catch (Exception exception) {
            throw new StorageException("Could not initialize MinIO bucket", exception);
        }
    }

    @Override
    public StoredObject put(String storageKey, InputStream content, long sizeBytes, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(storageKey)
                    .stream(content, sizeBytes, -1L)
                    .contentType(contentType)
                    .build());
            return new StoredObject(storageKey, sizeBytes, contentType);
        } catch (Exception exception) {
            throw new StorageException("Could not upload object to MinIO", exception);
        }
    }

    @Override
    public InputStream get(String storageKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(storageKey)
                    .build());
        } catch (ErrorResponseException exception) {
            if ("NoSuchKey".equals(exception.errorResponse().code())) {
                throw new StorageObjectNotFoundException(storageKey, exception);
            }
            throw new StorageException("Could not download object from MinIO", exception);
        } catch (Exception exception) {
            throw new StorageException("Could not download object from MinIO", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(storageKey)
                    .build());
        } catch (Exception exception) {
            throw new StorageException("Could not delete object from MinIO", exception);
        }
    }
}
