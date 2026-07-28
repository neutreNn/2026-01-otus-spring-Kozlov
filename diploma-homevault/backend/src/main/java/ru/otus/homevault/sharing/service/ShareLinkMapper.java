package ru.otus.homevault.sharing.service;

import org.springframework.stereotype.Component;
import ru.otus.homevault.sharing.dto.ShareResponse;
import ru.otus.homevault.sharing.model.ShareLink;

@Component
public class ShareLinkMapper {

    public ShareResponse toResponse(ShareLink shareLink) {
        return new ShareResponse(
                shareLink.getId(),
                shareLink.getToken(),
                shareLink.getResourceType(),
                shareLink.getResourceId(),
                shareLink.getExpiresAt(),
                shareLink.getRevokedAt(),
                shareLink.getAccessCount(),
                shareLink.getCreatedAt()
        );
    }
}
