package com.example.social.app.business.service.message;

import com.example.social.app.business.dto.message.ConversationDTO;
import com.example.social.app.business.dto.message.MessageDTO;
import com.example.social.app.business.dto.message.MessageRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Map<String, Object> createOrGetConversation(String keycloakId, Long recipientId, String initialContent);

    MessageDTO sendMessage(String keycloakId, Long conversationId, String content);

    List<ConversationDTO> getConversations(String keycloakId);

    Page<MessageDTO> getMessages(String keycloakId, Long conversationId, int page, int size);

    ConversationDTO getConversation(String keycloakId, Long conversationId);

    MessageDTO updateMessage(String keycloakId, Long messageId, String content);

    void markMessagesAsRead(String keycloakId, Long conversationId);

    List<MessageRequestDTO> getMessageRequests(String keycloakId);

    ConversationDTO acceptMessageRequest(String keycloakId, Long requestId);

    void declineMessageRequest(String keycloakId, Long requestId);
}
