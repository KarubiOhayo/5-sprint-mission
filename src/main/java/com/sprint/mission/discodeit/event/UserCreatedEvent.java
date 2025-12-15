package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.user.UserDto;

import lombok.Getter;

@Getter
public class UserCreatedEvent extends CreatedEvent<UserDto> {

	private final String name = "users.created";

	public UserCreatedEvent(UserDto data, Instant createdAt) {
		super(data, createdAt);
	}
}
