package ru.otus.homevault.audit.service;

import jakarta.servlet.http.HttpServletRequest;

public record AuditContext(String ipAddress, String userAgent) {

    public static AuditContext from(HttpServletRequest request) {
        return new AuditContext(request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}
