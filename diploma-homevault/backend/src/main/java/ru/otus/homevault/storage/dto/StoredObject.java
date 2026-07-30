package ru.otus.homevault.storage.dto;

public record StoredObject(String storageKey, long sizeBytes, String contentType) {
}
