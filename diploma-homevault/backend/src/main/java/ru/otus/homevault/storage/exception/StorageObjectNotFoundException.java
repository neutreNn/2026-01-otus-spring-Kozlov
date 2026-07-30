package ru.otus.homevault.storage.exception;

public class StorageObjectNotFoundException extends StorageException {

    public StorageObjectNotFoundException(String storageKey, Throwable cause) {
        super("Storage object not found: " + storageKey, cause);
    }
}
