package ru.otus.homevault.storage.service;

import ru.otus.homevault.storage.dto.StoredObject;

import java.io.InputStream;

public interface FileStorageService {

    StoredObject put(String storageKey, InputStream content, long sizeBytes, String contentType);

    InputStream get(String storageKey);

    void delete(String storageKey);
}
