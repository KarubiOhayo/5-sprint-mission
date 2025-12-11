package com.sprint.mission.discodeit.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;

public interface NotificationService {

	void create(Set<UUID> receiverIds, String title, String content);

	List<NotificationDto> findByUserId(UUID userId);

	void delete(UUID id);
}
