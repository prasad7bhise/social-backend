package com.example.social.business.service.reaction;

import com.example.social.app.business.dto.messagereaction.MessageReactionDTO;
import com.example.social.app.business.service.messagereaction.impl.MessageReactionServiceImpl;
import com.example.social.app.db.dao.message.MessageRepository;
import com.example.social.app.db.dao.messagereaction.MessageReactionRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.message.MessageEntity;
import com.example.social.app.db.entity.messagereaction.MessageReactionEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.social.business.service.reaction.dataset.MessageReactionServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageReactionServiceImplUnitTest {

    @Mock
    private MessageReactionRepository reactionRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private MessageReactionServiceImpl messageReactionService;

    private UsersEntity currentUser;
    private MessageEntity existingMessage;

    @BeforeEach
    void setUp() {
        currentUser = currentUser();
        existingMessage = existingMessage();
    }

    @Test
    void test01_getReactions_shouldReturnReactions() {
        MessageReactionEntity reaction = existingReaction();
        when(reactionRepository.findByMessageId(50L)).thenReturn(List.of(reaction));

        List<MessageReactionDTO> result = messageReactionService.getReactions(50L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmoji()).isEqualTo("👍");
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void test02_getReactions_shouldReturnEmpty_whenNoReactions() {
        when(reactionRepository.findByMessageId(50L)).thenReturn(List.of());

        List<MessageReactionDTO> result = messageReactionService.getReactions(50L);

        assertThat(result).isEmpty();
    }

    @Test
    void test03_addReaction_shouldAddNewReaction() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(50L)).thenReturn(Optional.of(existingMessage));
        when(reactionRepository.existsByMessageIdAndUserIdAndEmoji(50L, 1L, "👍")).thenReturn(false);
        when(reactionRepository.save(any())).thenAnswer(invocation -> {
            MessageReactionEntity r = invocation.getArgument(0);
            r.setId(200L);
            return r;
        });

        MessageReactionDTO result = messageReactionService.addReaction("kc-current", 50L, "👍");

        assertThat(result.getEmoji()).isEqualTo("👍");
        verify(reactionRepository).save(any());
        verify(reactionRepository, never()).deleteByMessageIdAndUserIdAndEmoji(any(), any(), any());
    }

    @Test
    void test04_addReaction_shouldToggle_whenSameEmojiExists() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(50L)).thenReturn(Optional.of(existingMessage));
        when(reactionRepository.existsByMessageIdAndUserIdAndEmoji(50L, 1L, "👍")).thenReturn(true);
        when(reactionRepository.save(any())).thenAnswer(invocation -> {
            MessageReactionEntity r = invocation.getArgument(0);
            r.setId(200L);
            return r;
        });

        MessageReactionDTO result = messageReactionService.addReaction("kc-current", 50L, "👍");

        // Existing emoji should be removed first, then re-added (toggle-like behavior)
        verify(reactionRepository).deleteByMessageIdAndUserIdAndEmoji(50L, 1L, "👍");
        verify(reactionRepository).save(any());
    }

    @Test
    void test05_addReaction_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(usersRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageReactionService.addReaction("unknown", 50L, "👍"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void test06_addReaction_shouldThrowEntityNotFoundException_whenMessageNotFound() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageReactionService.addReaction("kc-current", 99L, "👍"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Message not found");
    }

    @Test
    void test07_removeReaction_shouldRemoveReaction() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));

        messageReactionService.removeReaction("kc-current", 50L, "👍");

        verify(reactionRepository).deleteByMessageIdAndUserIdAndEmoji(50L, 1L, "👍");
    }

    @Test
    void test08_removeReaction_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(usersRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageReactionService.removeReaction("unknown", 50L, "👍"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }
}
