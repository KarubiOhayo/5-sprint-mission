package com.sprint.mission.discodeit.controller;

import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateCommand;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {
	private final MessageService messageService;

	@MessageMapping("/messages")
	public void sendMessage(
		@Payload MessageCreateRequest request
	) {
		MessageCreateCommand command = new MessageCreateCommand(
			request.channelId(),
			request.authorId(),
			request.content(),
			List.of()
		);
		messageService.create(command);
	}
}
