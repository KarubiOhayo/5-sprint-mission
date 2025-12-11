package com.sprint.mission.discodeit.event;

import java.time.Instant;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;

import lombok.Getter;

@Getter
public class BinaryContentUpdatedEvent extends UpdatedEvent<BinaryContentDto> {

	private final String name = "binaryContents.updated";

	public BinaryContentUpdatedEvent(BinaryContentDto from, BinaryContentDto to, Instant updatedAt) {
		super(from, to, updatedAt);
	}
}
