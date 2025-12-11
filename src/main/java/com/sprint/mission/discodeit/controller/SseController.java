package com.sprint.mission.discodeit.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.sse.SseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {
	private final SseService sseService;

	@GetMapping
	public SseEmitter subscribe(
		@RequestParam(value = "lastEventId", required = false) String lastEventId,
		@AuthenticationPrincipal DiscodeitUserDetails userDetails
	) {
		UUID last = null;
		if (lastEventId != null && !lastEventId.isBlank()) {
			last = UUID.fromString(lastEventId);
		}

		return sseService.connect(userDetails.getUserId(), last);
	}
}
