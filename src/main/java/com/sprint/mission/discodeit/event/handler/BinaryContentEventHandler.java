package com.sprint.mission.discodeit.event.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.sse.SseService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventHandler {
	private final BinaryContentStorage binaryContentStorage;
	private final SseService sseService;

	@Async("taskExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleAfterCommitCreate(BinaryContentCreatedEvent event) {
		log.info("[BinaryContent Event] 파일 메타데이터 생성 감지: {}", event.binaryContentId());
		binaryContentStorage.put(event.binaryContentId(), event.file());
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleAfterCommitUpdate(BinaryContentUpdatedEvent event) {
		log.info("[BinaryContent Event] 파일 업로드 상태 변경 감지");
		sseService.broadcast(event.getName(), event.getTo());
	}
}
