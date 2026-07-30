package ru.otus.homevault.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "System statistics")
public record AdminStatsResponse(
        @Schema(description = "Total users count", example = "42")
        long totalUsers,

        @Schema(description = "Active users count", example = "40")
        long activeUsers,

        @Schema(description = "Blocked users count", example = "2")
        long blockedUsers,

        @Schema(description = "Folders count", example = "120")
        long foldersCount,

        @Schema(description = "Files count", example = "512")
        long filesCount,

        @Schema(description = "Total stored file size in bytes", example = "1073741824")
        long totalStorageBytes,

        @Schema(description = "Notes count", example = "180")
        long notesCount,

        @Schema(description = "Share links count", example = "31")
        long shareLinksCount,

        @Schema(description = "Active share links count", example = "12")
        long activeShareLinks
) {
}
