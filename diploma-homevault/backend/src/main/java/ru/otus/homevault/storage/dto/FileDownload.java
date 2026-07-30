package ru.otus.homevault.storage.dto;

import java.io.InputStream;
import java.util.UUID;

public record FileDownload(
        UUID id,
        String originalName,
        String contentType,
        long sizeBytes,
        InputStream content
) {
}
