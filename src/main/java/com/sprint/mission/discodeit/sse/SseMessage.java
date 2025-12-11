package com.sprint.mission.discodeit.sse;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

	public Set<ResponseBodyEmitter.DataWithMediaType> toEvent() {
		SseEmitter.SseEventBuilder builder = SseEmitter.event()
			.id(eventId.toString())
			.name(eventName)
			.data(data);

		return Set.of(new ResponseBodyEmitter.DataWithMediaType(builder, MediaType.APPLICATION_JSON));
	}
}
