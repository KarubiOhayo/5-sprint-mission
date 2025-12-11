package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;

import lombok.Getter;

@Getter
public class NotificationCreatedEvent extends CreatedEvent<NotificationDto> {

	private final String name = "notifications.created";

	public NotificationCreatedEvent(NotificationDto data, Instant createdAt) {
		super(data, createdAt);
	}
}
