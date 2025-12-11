package com.sprint.mission.discodeit.service.basic;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final RedisCacheManager cacheManager;
	private final ApplicationEventPublisher eventPublisher;
	private final NotificationMapper notificationMapper;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void create(Set<UUID> receiverIds, String title, String content) {
		if (receiverIds.isEmpty()) {
			log.warn("No receivers found for request: {}", receiverIds);
			return;
		}
		log.debug("Creating notification: {}", receiverIds);
		List<Notification> notifications = receiverIds.stream()
			.map(id -> new Notification(
				id,
				title,
				content
			))
			.toList();
		List<NotificationDto> dtos = notificationRepository.saveAll(notifications).stream()
			.map(notificationMapper::toDto)
			.toList();
		for (NotificationDto dto : dtos) {
			eventPublisher.publishEvent(new NotificationCreatedEvent(dto, dto.createdAt()));
		}
		evictNotificationCache(receiverIds);
		log.info("Created notification: {}", notifications);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "notifications", key = "#userId")
	public List<NotificationDto> findByUserId(UUID userId) {
		if (!userRepository.existsById(userId)) {
			throw new UserNotFoundException().addDetail("userId", userId);
		}
		return notificationRepository.findAllByReceiverId(userId);
	}

	@Transactional(readOnly = true)
	public boolean isOwner(UUID notificationId, UUID userId) {
		return notificationRepository.findById(notificationId)
			.map(Notification::getReceiverId)
			.filter(userId::equals)
			.isPresent();
	}

	@Override
	@Transactional
	@PreAuthorize("@basicNotificationService.isOwner(#id, principal.userDto.id)")
	@CacheEvict(value = "notifications", allEntries = true)
	public void delete(UUID id) {
		if (!notificationRepository.existsById(id)) {
			throw new DiscodeitException(ErrorCode.NOTIFICATION_NOT_FOUND).addDetail("notificationId", id);
		}
		notificationRepository.deleteById(id);
	}

	private void evictNotificationCache(Set<UUID> notificationIds) {
		Cache cache = cacheManager.getCache("notifications");
		if (cache != null) {
			for (UUID notificationId : notificationIds) {
				cache.evict(notificationId);
			}
			log.debug("Evicting notification cache for notificationIds: {}", notificationIds);
		} else {
			log.warn("No cache found for notificationIds");
		}
	}
}
