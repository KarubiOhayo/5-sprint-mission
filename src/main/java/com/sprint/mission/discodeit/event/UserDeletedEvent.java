package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.user.UserDto;

import lombok.Getter;

@Getter
public class UserDeletedEvent extends DeletedEvent<UserDto> {

	private final String name = "users.deleted";

	public UserDeletedEvent(UserDto data, Instant deletedAt) {
		super(data, deletedAt);
	}
}
