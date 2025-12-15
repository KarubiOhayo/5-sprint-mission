package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.sprint.mission.discodeit.dto.user.JwtInformation;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.event.UserUpdatedEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry<UUID> {

	private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
	private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
	private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

	private final int maxActiveJwtCount;
	private final JwtTokenProvider jwtTokenProvider;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	@Transactional
	public void registerJwtInformation(JwtInformation jwtInformation) {
		origin.compute(jwtInformation.getUserDto().id(), (key, queue) -> {
			if (queue == null) {
				queue = new ConcurrentLinkedQueue<>();
			}
			if (queue.size() >= maxActiveJwtCount) {
				JwtInformation deprecatedJwtInformation = queue.poll();
				if (deprecatedJwtInformation != null) {
					removeTokenIndex(
						deprecatedJwtInformation.getAccessToken(),
						deprecatedJwtInformation.getRefreshToken()
					);
				}
			}

			queue.add(jwtInformation);
			addTokenIndex(
				jwtInformation.getAccessToken(),
				jwtInformation.getRefreshToken()
			);
			return queue;
		});
		UserDto prevUserDto = jwtInformation.getUserDto();
		UserDto newUserDto = new UserDto(
			prevUserDto.id(),
			prevUserDto.username(),
			prevUserDto.email(),
			prevUserDto.profile(),
			true,
			prevUserDto.role()
		);
		eventPublisher.publishEvent(new UserUpdatedEvent(prevUserDto, newUserDto, Instant.now()));
	}

	@Override
	@Transactional
	public void invalidateJwtInformationByUserId(UUID userId) {
		AtomicReference<UserDto> captured = new AtomicReference<>();
		origin.computeIfPresent(userId, (key, queue) -> {
			JwtInformation first = queue.peek();
			if (first != null) {
				captured.set(first.getUserDto());
			}
			queue.forEach(jwtInformation -> removeTokenIndex(
				jwtInformation.getAccessToken(),
				jwtInformation.getRefreshToken()
			));
			queue.clear();
			return null;

		});
		UserDto prevUserDto = captured.get();

		if (prevUserDto != null) {
			UserDto newUserDto = new UserDto(
				prevUserDto.id(),
				prevUserDto.username(),
				prevUserDto.email(),
				prevUserDto.profile(),
				false,
				prevUserDto.role()
			);
			eventPublisher.publishEvent(new UserUpdatedEvent(prevUserDto, newUserDto, Instant.now()));
		}
	}

	@Override
	public boolean hasActiveJwtInformationByUserId(UUID userId) {
		return origin.containsKey(userId);
	}

	@Override
	public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
		return accessTokenIndexes.contains(accessToken);
	}

	@Override
	public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
		return refreshTokenIndexes.contains(refreshToken);
	}

	@Override
	public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
		origin.computeIfPresent(newJwtInformation.getUserDto().id(), (key, queue) -> {
			queue.stream().filter(jwtInformation -> jwtInformation.getRefreshToken().equals(refreshToken))
				.findFirst()
				.ifPresent(jwtInformation -> {
					removeTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
					jwtInformation.rotate(
						newJwtInformation.getAccessToken(),
						newJwtInformation.getRefreshToken()
					);
					addTokenIndex(
						newJwtInformation.getAccessToken(),
						newJwtInformation.getRefreshToken()
					);
				});
			return queue;
		});
	}

	@Scheduled(fixedDelay = 1000 * 60 * 5)
	@Override
	public void clearExpiredJwtInformation() {
		origin.entrySet().removeIf(entry -> {
			Queue<JwtInformation> queue = entry.getValue();
			queue.removeIf(jwtInformation -> {
				boolean isExpired =
					!jwtTokenProvider.validateAccessToken(jwtInformation.getAccessToken()) ||
						!jwtTokenProvider.validateRefreshToken(jwtInformation.getRefreshToken());
				if (isExpired) {
					removeTokenIndex(
						jwtInformation.getAccessToken(),
						jwtInformation.getRefreshToken()
					);
				}
				return isExpired;
			});
			return queue.isEmpty();
		});
	}

	private void addTokenIndex(String accessToken, String refreshToken) {
		accessTokenIndexes.add(accessToken);
		refreshTokenIndexes.add(refreshToken);
	}

	private void removeTokenIndex(String accessToken, String refreshToken) {
		accessTokenIndexes.remove(accessToken);
		refreshTokenIndexes.remove(refreshToken);
	}
}
