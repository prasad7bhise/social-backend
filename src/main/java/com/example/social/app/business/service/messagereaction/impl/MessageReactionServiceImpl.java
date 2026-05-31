package com.example.social.app.business.service.messagereaction.impl;

import com.example.social.app.business.dto.messagereaction.MessageReactionDTO;
import com.example.social.app.business.service.messagereaction.MessageReactionService;
import com.example.social.app.db.dao.message.MessageRepository;
import com.example.social.app.db.dao.messagereaction.MessageReactionRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.message.MessageEntity;
import com.example.social.app.db.entity.messagereaction.MessageReactionEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageReactionServiceImpl implements MessageReactionService {

    private final MessageReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MessageReactionDTO> getReactions(Long messageId) {
        return reactionRepository.findByMessageId(messageId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageReactionDTO addReaction(String keycloakId, Long messageId, String emoji) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (reactionRepository.existsByMessageIdAndUserIdAndEmoji(messageId, user.getId(), emoji)) {
            reactionRepository.deleteByMessageIdAndUserIdAndEmoji(messageId, user.getId(), emoji);
        }

        MessageReactionEntity reaction = new MessageReactionEntity();
        reaction.setMessage(message);
        reaction.setUser(user);
        reaction.setEmoji(emoji);
        reactionRepository.save(reaction);

        return toDTO(reaction);
    }

    @Override
    @Transactional
    public void removeReaction(String keycloakId, Long messageId, String emoji) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        reactionRepository.deleteByMessageIdAndUserIdAndEmoji(messageId, user.getId(), emoji);
    }

    private MessageReactionDTO toDTO(MessageReactionEntity entity) {
        return new MessageReactionDTO(
                entity.getId(),
                entity.getUser().getId(),
                entity.getEmoji(),
                entity.getUser().getFirstName()
        );
    }
}
