package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;

import lombok.Getter;

@Getter
public class ChannelUpdatedEvent extends UpdatedEvent<ChannelDto> {

	private final String name = "channels.updated";

	public ChannelUpdatedEvent(ChannelDto from, ChannelDto to, Instant updatedAt) {
		super(from, to, updatedAt);
	}
}
