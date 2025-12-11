package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;

import lombok.Getter;

@Getter
public class ChannelCreatedEvent extends CreatedEvent<ChannelDto> {

	private final String name = "channels.created";

	public ChannelCreatedEvent(ChannelDto data, Instant createdAt) {
		super(data, createdAt);
	}
}
