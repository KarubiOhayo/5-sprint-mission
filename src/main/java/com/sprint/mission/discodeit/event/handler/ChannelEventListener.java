package com.sprint.mission.discodeit.event.handler;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.event.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.sse.SseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChannelEventListener {
	private final SseService sseService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleChannelCreatedEvent(ChannelCreatedEvent event) {
		Set<UUID> receiverIds = getReceiverIds(event.getData());
		if (receiverIds.isEmpty()) {
			sseService.broadcast(event.getName(), event.getData());
		} else {
			sseService.send(receiverIds, event.getName(), event.getData());
		}
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleChannelUpdatedEvent(ChannelUpdatedEvent event) {
		sseService.broadcast(event.getName(), event.getTo());
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleChannelDeletedEvent(ChannelDeletedEvent event) {
		Set<UUID> receiverIds = getReceiverIds(event.getData());
		if (receiverIds.isEmpty()) {
			sseService.broadcast(event.getName(), event.getData());
		} else {
			sseService.send(receiverIds, event.getName(), event.getData());
		}
	}

	private Set<UUID> getReceiverIds(ChannelDto dto) {
		return dto.participants().stream()
			.map(UserDto::id)
			.collect(Collectors.toSet());
	}
}
