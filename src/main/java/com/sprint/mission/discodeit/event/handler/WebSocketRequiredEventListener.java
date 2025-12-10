package com.sprint.mission.discodeit.event.handler;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {
	private final SimpMessagingTemplate messagingTemplate;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMessage(MessageCreatedEvent event) {
		messagingTemplate.convertAndSend(
			"/sub/channels." + event.messageDto().channelId() + ".messages",
			event.messageDto()
		);
	}
}
