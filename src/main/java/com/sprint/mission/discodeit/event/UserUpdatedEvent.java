package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.user.UserDto;

import lombok.Getter;

@Getter
public class UserUpdatedEvent extends UpdatedEvent<UserDto> {

	private final String name = "users.updated";

	public UserUpdatedEvent(UserDto from, UserDto to, Instant updatedAt) {
		super(from, to, updatedAt);
	}
}
