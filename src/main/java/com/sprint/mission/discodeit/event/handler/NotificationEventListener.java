package com.sprint.mission.discodeit.event.handler;

import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.sse.SseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
	private final SseService sseService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleNotificationCreatedEvent(NotificationCreatedEvent event) {
		log.info("[Notification Event] Received notification: {}", event);
		sseService.send(
			Set.of(event.getData().receiverId()),
			event.getName(),
			event.getData()
		);
	}
}
