package com.sprint.mission.discodeit.sse;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

	@Value("${sse.timeout:1800000}") // 기본 30분
	private long timeout;

	private final SseEmitterRepository sseEmitterRepository;
	private final SseMessageRepository sseMessageRepository;

	public SseEmitter connect(UUID receiverId, UUID lastEventId) {
		SseEmitter emitter = new SseEmitter(timeout);

		emitter.onCompletion(() -> sseEmitterRepository.delete(receiverId, emitter));
		emitter.onTimeout(() -> sseEmitterRepository.delete(lastEventId, emitter));
		emitter.onError((e) -> sseEmitterRepository.delete(receiverId, emitter));

		sseEmitterRepository.save(receiverId, emitter);

		Optional.ofNullable(lastEventId)
			.ifPresentOrElse(
				id -> sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, receiverId)
					.forEach(msg -> {
						try {
							emitter.send(
								SseEmitter.event()
									.id(msg.getEventId().toString())
									.name(msg.getEventName())
									.data(msg.getData())
							);
						} catch (Exception e) {
							log.error("[SseService#connect] Failed to send message: {}", e.getMessage());
							emitter.completeWithError(e);
						}
					}),
				() -> ping(emitter)
			);
		return emitter;
	}

	public void send(Collection<UUID> receiverIds, String eventName, Object data) {
		SseMessage message = sseMessageRepository.save(
			SseMessage.create(receiverIds, eventName, data));

		List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverIdsIn(receiverIds);

		log.debug("[SseService#send] SSE send. eventName={} receivers={} emitterCount={}",
			eventName, receiverIds, emitters.size());

		emitters.forEach(emitter -> {
			try {
				emitter.send(
					SseEmitter.event()
						.id(message.getEventId().toString())
						.name(eventName)
						.data(data)
				);
			} catch (Exception e) {
				log.error("[SseService#send] Failed to send message: {}", e.getMessage());
				emitter.completeWithError(e);
			}
		});

	}

	public void broadcast(String eventName, Object data) {
		log.debug("[SseService#broadcast] SSE broadcast. eventName={}", eventName);
		SseMessage message = sseMessageRepository.save(
			SseMessage.createBroadcast(eventName, data));
		sseEmitterRepository.findAll().forEach(emitter -> {
			try {
				emitter.send(SseEmitter.event()
					.id(message.getEventId().toString())
					.name(message.getEventName())
					.data(data));
			} catch (Exception e) {
				log.error("[SseService#broadcast] Failed to send message: {}", e.getMessage());
				emitter.completeWithError(e);
			}
		});
	}

	@Scheduled(fixedDelay = 1000 * 60 * 30)
	public void cleanUp() {
		sseEmitterRepository.findAll().stream()
			.filter(emitter -> !ping(emitter))
			.forEach(ResponseBodyEmitter::complete);
	}

	private boolean ping(SseEmitter sseEmitter) {
		try {
			sseEmitter.send(SseEmitter.event().name("ping").build());
			return true;
		} catch (IOException | IllegalStateException e) {
			log.debug("[SseService#ping] Failed to send ping event: {}", e.getMessage());
			return false;
		}
	}
}
