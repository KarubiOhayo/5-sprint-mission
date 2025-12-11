package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;

import lombok.Getter;

@Getter
public class ChannelDeletedEvent extends DeletedEvent<ChannelDto> {

	private final String name = "channels.deleted";

	public ChannelDeletedEvent(ChannelDto data, Instant deletedAt) {
		super(data, deletedAt);
	}
}
