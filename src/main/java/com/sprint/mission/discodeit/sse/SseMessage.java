package com.sprint.mission.discodeit.sse;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;

@Getter
public class SseMessage {
	private final UUID eventId;
	private final String eventName;
	private final Object data;
	private final Set<UUID> receiverIds;
	private final boolean broadcast;

	public SseMessage(String eventName, Object data, Set<UUID> receiverIds, boolean broadcast) {
		this.eventId = UUID.randomUUID();
		this.eventName = eventName;
		this.data = data;
		this.receiverIds = receiverIds;
		this.broadcast = broadcast;
	}

	public static SseMessage create(Collection<UUID> receiverIds, String eventName, Object data) {
		return new SseMessage(eventName, data, Set.copyOf(receiverIds), false);
	}

	public static SseMessage createBroadcast(String eventName, Object data) {
		return new SseMessage(eventName, data, Set.of(), true);
	}

	public boolean isReceivable(UUID receiverId) {
		return broadcast || receiverIds.contains(receiverId);
	}
}
