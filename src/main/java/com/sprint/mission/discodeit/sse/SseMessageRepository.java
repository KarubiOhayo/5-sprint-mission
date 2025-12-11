package com.sprint.mission.discodeit.sse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

	@Value("${sse.event-queue-capacity:100}")
	private int eventQueueCapacity;

	private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
	private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

	public SseMessage save(SseMessage sseMessage) {
		makeAvailableCapacity();

		UUID eventId = sseMessage.getEventId();
		eventIdQueue.offer(eventId);
		messages.put(eventId, sseMessage);
		return sseMessage;
	}

	public List<SseMessage> findAllByEventIdAfterAndReceiverId(UUID eventId, UUID receiverId) {
		return eventIdQueue.stream()
			.dropWhile(id -> !id.equals(eventId))
			.skip(1)
			.map(messages::get)
			.filter(msg -> msg != null && msg.isReceivable(receiverId))
			.toList();
	}

	private void makeAvailableCapacity() {
		int availableCapacity = eventQueueCapacity - eventIdQueue.size();
		while (availableCapacity < 1) {
			UUID removedId = eventIdQueue.poll();
			messages.remove(removedId);
			availableCapacity++;
		}
	}
}
