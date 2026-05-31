package com.example.social.business.service.feed;

import com.example.social.app.business.dto.feed.*;
import com.example.social.app.business.mapper.FeedMapper;
import com.example.social.app.business.service.feed.impl.FeedServiceImpl;
import com.example.social.app.business.service.notification.NotificationService;
import com.example.social.app.db.dao.follow.FollowRepository;
import com.example.social.app.db.dao.post.CommentRepository;
import com.example.social.app.db.dao.post.PostLikesRepository;
import com.example.social.app.db.dao.post.PostRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.post.CommentsEntity;
import com.example.social.app.db.entity.post.PostEntity;
import com.example.social.app.db.entity.post.PostLikesEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.MediaType;
import com.example.social.app.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.social.business.service.feed.dataset.FeedServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceImplUnitTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostLikesRepository postLikesRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FeedServiceImpl feedService;

    private UsersEntity currentUser;
    private UsersEntity postAuthor;
    private PostEntity existingPost;
    private PostEntity userOwnPost;

    @BeforeEach
    void setUp() {
        currentUser = currentUser();
        postAuthor = postAuthor();
        existingPost = existingPost();
        userOwnPost = userOwnPost();
    }

    @Test
    void test01_getFeed_shouldReturnFollowedUsersPosts() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(followRepository.findFollowingUserIdsByFollower(currentUser)).thenReturn(List.of(2L));
        when(postRepository.findByUserIdInOrderByCreatedAtDesc(eq(List.of(2L)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existingPost)));

        when(feedMapper.toUserBrief(postAuthor)).thenReturn(authorBrief());
        when(feedMapper.toMediaDTOList(any())).thenReturn(List.of(new MediaDTO()));
        when(postLikesRepository.countByPost(any())).thenReturn(0L);
        when(postLikesRepository.existsByPostAndUser(any(), any())).thenReturn(false);
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(feedMapper.toPostDTO(any(), any(), anyList(), anyLong(), anyLong(), anyBoolean(), anyList()))
                .thenReturn(new PostDTO());

        Page<PostDTO> result = feedService.getFeed(0, 10, "kc-current");

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void test02_getFeed_shouldFallbackToAllPosts_whenNotFollowingAnyone() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(followRepository.findFollowingUserIdsByFollower(currentUser)).thenReturn(List.of());
        when(postRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existingPost)));

        when(feedMapper.toUserBrief(postAuthor)).thenReturn(authorBrief());
        when(feedMapper.toMediaDTOList(any())).thenReturn(List.of(new MediaDTO()));
        when(postLikesRepository.countByPost(any())).thenReturn(0L);
        when(postLikesRepository.existsByPostAndUser(any(), any())).thenReturn(false);
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(feedMapper.toPostDTO(any(), any(), anyList(), anyLong(), anyLong(), anyBoolean(), anyList()))
                .thenReturn(new PostDTO());

        Page<PostDTO> result = feedService.getFeed(0, 10, "kc-current");

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void test03_getFeed_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(usersRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> feedService.getFeed(0, 10, "unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void test04_getExplore_shouldReturnAllPosts() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existingPost)));

        when(feedMapper.toUserBrief(postAuthor)).thenReturn(authorBrief());
        when(feedMapper.toMediaDTOList(any())).thenReturn(List.of(new MediaDTO()));
        when(postLikesRepository.countByPost(any())).thenReturn(0L);
        when(postLikesRepository.existsByPostAndUser(any(), any())).thenReturn(false);
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(feedMapper.toPostDTO(any(), any(), anyList(), anyLong(), anyLong(), anyBoolean(), anyList()))
                .thenReturn(new PostDTO());

        Page<PostDTO> result = feedService.getExplore(0, 10, "kc-current");

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void test05_getPost_shouldReturnPost() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(existingPost));

        when(feedMapper.toUserBrief(postAuthor)).thenReturn(authorBrief());
        when(feedMapper.toMediaDTOList(any())).thenReturn(List.of(new MediaDTO()));
        when(postLikesRepository.countByPost(any())).thenReturn(0L);
        when(postLikesRepository.existsByPostAndUser(any(), any())).thenReturn(false);
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(feedMapper.toPostDTO(any(), any(), anyList(), anyLong(), anyLong(), anyBoolean(), anyList()))
                .thenReturn(new PostDTO());

        PostDTO result = feedService.getPost(10L, "kc-current");

        assertThat(result).isNotNull();
    }

    @Test
    void test06_getPost_shouldThrowEntityNotFoundException_whenPostNotFound() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> feedService.getPost(99L, "kc-current"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void test07_createPost_shouldCreatePostWithMedia() {
        CreatePostRequest request = createPostRequest();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.save(any())).thenAnswer(invocation -> {
            PostEntity p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toMediaDTOList(any())).thenReturn(List.of(new MediaDTO()));
        when(postLikesRepository.countByPost(any())).thenReturn(0L);
        when(postLikesRepository.existsByPostAndUser(any(), any())).thenReturn(false);
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(feedMapper.toPostDTO(any(), any(), anyList(), anyLong(), anyLong(), anyBoolean(), anyList()))
                .thenReturn(new PostDTO());

        PostDTO result = feedService.createPost(request, "kc-current");

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        PostEntity saved = captor.getValue();
        assertThat(saved.getContent()).isEqualTo("New post");
        assertThat(saved.getUser()).isEqualTo(currentUser);
        assertThat(saved.getMedia()).hasSize(1);
        assertThat(saved.getMedia().get(0).getType()).isEqualTo(MediaType.IMAGE);
    }

    @Test
    void test08_createPost_shouldCreatePostWithoutMedia() {
        CreatePostRequest request = createPostRequestWithoutMedia();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.save(any())).thenAnswer(invocation -> {
            PostEntity p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toMediaDTOList(any())).thenReturn(List.of());
        when(postLikesRepository.countByPost(any())).thenReturn(0L);
        when(postLikesRepository.existsByPostAndUser(any(), any())).thenReturn(false);
        when(commentRepository.findByPostIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        doReturn(new PostDTO()).when(feedMapper).toPostDTO(any(), any(), anyList(), anyLong(), anyLong(), anyBoolean(), anyList());

        feedService.createPost(request, "kc-current");

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getMedia()).isNullOrEmpty();
    }

    @Test
    void test09_deletePost_shouldDeleteOwnPost() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(11L)).thenReturn(Optional.of(userOwnPost));

        feedService.deletePost(11L, "kc-current");

        verify(postRepository).delete(userOwnPost);
    }

    @Test
    void test10_deletePost_shouldThrowSecurityException_whenNotOwnPost() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(existingPost));

        assertThatThrownBy(() -> feedService.deletePost(10L, "kc-current"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You can only delete your own posts");
    }

    @Test
    void test11_toggleLike_shouldAddNewLike_andNotifyPostAuthor() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(existingPost));
        when(postLikesRepository.findByPostAndUser(existingPost, currentUser)).thenReturn(Optional.empty());
        existingPost.getUser().setId(2L);

        feedService.toggleLike(10L, "kc-current");

        ArgumentCaptor<PostLikesEntity> captor = ArgumentCaptor.forClass(PostLikesEntity.class);
        verify(postLikesRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isEqualTo(existingPost);
        assertThat(captor.getValue().getUser()).isEqualTo(currentUser);
        verify(notificationService).createNotification(
                2L, 1L, NotificationType.LIKE, 10L, "Current liked your post"
        );
    }

    @Test
    void test12_toggleLike_shouldRemoveExistingLike() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(existingPost));
        PostLikesEntity existingLike = new PostLikesEntity();
        existingLike.setId(1L);
        when(postLikesRepository.findByPostAndUser(existingPost, currentUser)).thenReturn(Optional.of(existingLike));

        feedService.toggleLike(10L, "kc-current");

        verify(postLikesRepository).delete(existingLike);
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    void test13_toggleLike_shouldNotNotify_whenLikingOwnPost() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(11L)).thenReturn(Optional.of(userOwnPost));
        when(postLikesRepository.findByPostAndUser(userOwnPost, currentUser)).thenReturn(Optional.empty());

        feedService.toggleLike(11L, "kc-current");

        verify(postLikesRepository).save(any());
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    void test14_addComment_shouldAddCommentAndNotify() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(10L)).thenReturn(Optional.of(existingPost));
        when(commentRepository.save(any())).thenAnswer(invocation -> {
            CommentsEntity c = invocation.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toCommentDTO(any(), any())).thenReturn(new CommentDTO());

        CommentDTO result = feedService.addComment(10L, createCommentRequest(), "kc-current");

        assertThat(result).isNotNull();
        ArgumentCaptor<CommentsEntity> captor = ArgumentCaptor.forClass(CommentsEntity.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("Great post!");
        verify(notificationService).createNotification(
                2L, 1L, NotificationType.COMMENT, 10L, "Current commented on your post"
        );
    }

    @Test
    void test15_addComment_shouldNotNotify_whenCommentingOwnPost() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(postRepository.findById(11L)).thenReturn(Optional.of(userOwnPost));
        when(commentRepository.save(any())).thenAnswer(invocation -> {
            CommentsEntity c = invocation.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toCommentDTO(any(), any())).thenReturn(new CommentDTO());

        feedService.addComment(11L, createCommentRequest(), "kc-current");

        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    void test16_deleteComment_shouldDeleteOwnComment() {
        CommentsEntity ownComment = userOwnComment();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(commentRepository.findById(21L)).thenReturn(Optional.of(ownComment));

        feedService.deleteComment(21L, "kc-current");

        verify(commentRepository).delete(ownComment);
    }

    @Test
    void test17_deleteComment_shouldThrowSecurityException_whenNotOwnComment() {
        CommentsEntity othersComment = existingComment();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(commentRepository.findById(20L)).thenReturn(Optional.of(othersComment));

        assertThatThrownBy(() -> feedService.deleteComment(20L, "kc-current"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You can only delete your own comments");
    }

    @Test
    void test18_updateComment_shouldUpdateOwnComment() {
        CommentsEntity ownComment = userOwnComment();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(commentRepository.findById(21L)).thenReturn(Optional.of(ownComment));
        when(commentRepository.save(any())).thenReturn(ownComment);
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toCommentDTO(any(), any())).thenReturn(new CommentDTO());

        CommentDTO result = feedService.updateComment(21L, "Updated content", "kc-current");

        assertThat(result).isNotNull();
        assertThat(ownComment.getContent()).isEqualTo("Updated content");
    }

    @Test
    void test19_updateComment_shouldThrowSecurityException_whenNotOwnComment() {
        CommentsEntity othersComment = existingComment();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(commentRepository.findById(20L)).thenReturn(Optional.of(othersComment));

        assertThatThrownBy(() -> feedService.updateComment(20L, "Updated", "kc-current"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You can only edit your own comments");
    }

    @Test
    void test20_updateComment_shouldThrowSecurityException_whenEditWindowExpired() {
        CommentsEntity oldComment = oldComment();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(commentRepository.findById(22L)).thenReturn(Optional.of(oldComment));

        assertThatThrownBy(() -> feedService.updateComment(22L, "Updated", "kc-current"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Comments can only be edited within 15 minutes of posting");
    }
}
