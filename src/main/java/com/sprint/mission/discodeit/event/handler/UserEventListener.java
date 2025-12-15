package com.sprint.mission.discodeit.event.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.mission.discodeit.event.UserCreatedEvent;
import com.sprint.mission.discodeit.event.UserDeletedEvent;
import com.sprint.mission.discodeit.event.UserUpdatedEvent;
import com.sprint.mission.discodeit.sse.SseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

	private final SseService sseService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserCreatedEvent(UserCreatedEvent event) {
		sseService.broadcast(event.getName(), event.getData());
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserUpdatedEvent(UserUpdatedEvent event) {
		log.info("[AuthService#updateUser] success dto={}", event);
		sseService.broadcast(event.getName(), event.getTo());
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserDeletedEvent(UserDeletedEvent event) {
		sseService.broadcast(event.getName(), event.getData());
	}
}
